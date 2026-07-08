package app.gymly.service.membership

import app.gymly.dto.membership.EnrollRequest
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.exception.MembershipAlreadyActiveException
import app.gymly.exception.MembershipPendingApprovalException
import app.gymly.exception.NoActiveMembershipException
import app.gymly.exception.PlanInactiveException
import app.gymly.exception.PlanNotFoundException
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PaymentRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.presentation.MembershipViewService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MembershipEnrollmentService(
    private val membershipRepository: MembershipRepository,
    private val planRepository: PlanRepository,
    private val paymentRepository: PaymentRepository,
    private val membershipViewService: MembershipViewService,
    private val discountPricingService: DiscountPricingService,
) {
    @Transactional
    fun enroll(
        userId: Int,
        request: EnrollRequest,
    ): MyMembershipResponse {
        val planId = request.planId!!
        val plan = planRepository.findByIdOrNull(planId) ?: throw PlanNotFoundException(planId)
        if (!plan.active) throw PlanInactiveException(planId)

        val latest = membershipRepository.findFirstByUserIdOrderByIdDesc(userId)
        if (latest != null) {
            if (latest.status == MembershipStatus.PENDING) throw MembershipPendingApprovalException()
            if (latest.status == MembershipStatus.ACTIVE && !latest.endDate.isBefore(LocalDate.now())) {
                throw MembershipAlreadyActiveException()
            }
        }

        val today = LocalDate.now()
        val membership =
            membershipRepository.save(
                Membership(
                    userId = userId,
                    planId = planId,
                    initDate = today,
                    endDate = today.plusDays(plan.durationDays.toLong()),
                    status = MembershipStatus.PENDING,
                ),
            )

        if (request.paymentMethod == PaymentMethod.CASH || request.paymentMethod == PaymentMethod.TRANSFER) {
            val discount = discountPricingService.currentDiscountFor(planId)
            paymentRepository.save(
                Payment(
                    membershipId = membership.id!!,
                    userId = userId,
                    discountId = discount?.id,
                    amount = discount?.let { discountPricingService.discountedPrice(plan.price, it) } ?: plan.price,
                    method = request.paymentMethod,
                    status = PaymentStatus.PENDING,
                    reference = "Inscripción plan ${plan.name}",
                ),
            )
        }

        return membershipViewService.getMyMembership(userId)
    }

    @Transactional
    fun changePaymentMethod(
        userId: Int,
        method: PaymentMethod,
    ): MyMembershipResponse {
        val current =
            membershipRepository.findFirstByUserIdOrderByIdDesc(userId)
                ?: throw NoActiveMembershipException()

        if (current.status != MembershipStatus.PENDING) {
            throw NoActiveMembershipException()
        }

        val plan = planRepository.findByIdOrNull(current.planId) ?: throw PlanNotFoundException(current.planId)

        val pendingPayment =
            paymentRepository
                .findByMembershipId(current.id!!)
                .firstOrNull { it.status == PaymentStatus.PENDING }

        if (pendingPayment != null) {
            pendingPayment.method = method
            pendingPayment.amount = plan.price
            paymentRepository.save(pendingPayment)
        } else {
            paymentRepository.save(
                Payment(
                    membershipId = current.id!!,
                    userId = userId,
                    amount = plan.price,
                    method = method,
                    status = PaymentStatus.PENDING,
                    reference = "Inscripción plan ${plan.name}",
                ),
            )
        }

        return membershipViewService.getMyMembership(userId)
    }

    @Transactional
    fun cancel(userId: Int): MyMembershipResponse {
        val current =
            membershipRepository.findFirstByUserIdOrderByIdDesc(userId)
                ?: throw NoActiveMembershipException()

        if (current.status != MembershipStatus.ACTIVE && current.status != MembershipStatus.PENDING) {
            throw NoActiveMembershipException()
        }

        current.status = MembershipStatus.EXPIRED
        membershipRepository.save(current)

        paymentRepository
            .findByMembershipId(current.id!!)
            .filter { it.status == PaymentStatus.PENDING }
            .forEach {
                it.status = PaymentStatus.REJECTED
                paymentRepository.save(it)
            }

        return membershipViewService.getMyMembership(userId)
    }
}
