package app.gymly.service.membership

import app.gymly.dto.membership.EnrollRequest
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.exception.MembershipAlreadyActiveException
import app.gymly.exception.NoActiveMembershipException
import app.gymly.exception.PlanInactiveException
import app.gymly.exception.PlanNotFoundException
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.Payment
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
) {
    @Transactional
    fun enroll(
        userId: Int,
        request: EnrollRequest,
    ): MyMembershipResponse {
        val planId = request.planId!!
        val plan = planRepository.findByIdOrNull(planId) ?: throw PlanNotFoundException(planId)
        if (!plan.active) throw PlanInactiveException(planId)

        if (hasActiveMembership(userId)) throw MembershipAlreadyActiveException()

        val today = LocalDate.now()
        val membership =
            membershipRepository.save(
                Membership(
                    userId = userId,
                    planId = planId,
                    initDate = today,
                    endDate = today.plusDays(plan.durationDays.toLong()),
                    status = MembershipStatus.ACTIVE,
                ),
            )

        paymentRepository.save(
            Payment(
                membershipId = membership.id!!,
                userId = userId,
                amount = plan.price,
                method = request.paymentMethod!!,
                status = PaymentStatus.PENDING,
                reference = "Inscripción plan ${plan.name}",
            ),
        )

        return membershipViewService.getMyMembership(userId)
    }

    @Transactional
    fun cancel(userId: Int): MyMembershipResponse {
        val current =
            membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId)
                ?: throw NoActiveMembershipException()

        if (current.status != MembershipStatus.ACTIVE) throw NoActiveMembershipException()

        current.status = MembershipStatus.EXPIRED
        membershipRepository.save(current)

        return membershipViewService.getMyMembership(userId)
    }

    private fun hasActiveMembership(userId: Int): Boolean {
        val current = membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId) ?: return false
        return current.status == MembershipStatus.ACTIVE && !current.endDate.isBefore(LocalDate.now())
    }
}
