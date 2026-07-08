package app.gymly.service.payment

import app.gymly.dto.payment.CheckoutResponse
import app.gymly.dto.payment.PaymentRequest
import app.gymly.dto.payment.PaymentResponse
import app.gymly.dto.payment.UpdatePaymentRequest
import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PaymentRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.membership.DiscountPricingService
import app.gymly.service.membership.MembershipManagementService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class PaymentManagementService(
    private val paymentRepository: PaymentRepository,
    private val membershipManagementService: MembershipManagementService,
    private val membershipRepository: MembershipRepository,
    private val planRepository: PlanRepository,
    private val mercadoPagoService: MercadoPagoService,
    private val discountPricingService: DiscountPricingService,
) {
    @Transactional(readOnly = true)
    fun getAllPayments(): List<PaymentResponse> = paymentRepository.findAll().map { toResponse(it) }

    @Transactional(readOnly = true)
    fun getPaymentById(id: Int): PaymentResponse {
        val payment =
            paymentRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Pago con ID $id no encontrado")
        return toResponse(payment)
    }

    @Transactional
    fun createPayment(paymentRequest: PaymentRequest): PaymentResponse {
        val paymentEntity = toEntity(paymentRequest)
        val savedPayment = paymentRepository.save(paymentEntity)
        if (savedPayment.method == PaymentMethod.CASH && savedPayment.status == PaymentStatus.SUCCESSFUL) {
            membershipManagementService.activateMembership(savedPayment.membershipId)
        }
        return toResponse(savedPayment)
    }

    @Transactional
    fun createCheckout(paymentRequest: PaymentRequest): CheckoutResponse {
        val membership =
            membershipRepository.findByIdOrNull(paymentRequest.membershipId!!)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía con ID ${paymentRequest.membershipId} no encontrada")

        val plan =
            planRepository.findByIdOrNull(membership.planId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan con ID ${membership.planId} no encontrado")

        val conceptName = "You are paying: ${plan.name}"
        val planReference = "Inscripción plan ${plan.name}"

        val discount = discountPricingService.currentDiscountFor(membership.planId)
        val effectiveAmount = discount?.let { discountPricingService.discountedPrice(plan.price, it) } ?: plan.price

        val paymentEntity =
            paymentRepository
                .findByMembershipId(membership.id!!)
                .firstOrNull { it.status == PaymentStatus.PENDING }
                ?.apply {
                    amount = effectiveAmount
                    discountId = discount?.id
                    method = paymentRequest.method
                    reference = planReference
                }
                ?: toEntity(paymentRequest).apply {
                    status = PaymentStatus.PENDING
                    reference = planReference
                    amount = effectiveAmount
                    discountId = discount?.id
                }

        // Referencia única por pago: usar el ID local como external_reference permitía
        // que pagos aprobados antiguos de la cuenta de Mercado Pago (con el mismo ID
        // pequeño) se confundieran con el intento actual y aprobaran pagos rechazados.
        if (paymentEntity.checkoutReference == null) {
            paymentEntity.checkoutReference = "gymly-${UUID.randomUUID()}"
        }

        val savedPayment = paymentRepository.save(paymentEntity)

        val checkoutUrl = mercadoPagoService.createPaymentLink(conceptName, savedPayment.amount, savedPayment.checkoutReference!!)

        return CheckoutResponse(
            paymentId = savedPayment.id ?: 0,
            checkoutUrl = checkoutUrl,
        )
    }

    @Transactional
    fun updatePayment(
        id: Int,
        updatePaymentRequest: UpdatePaymentRequest,
    ): PaymentResponse {
        val existingPayment =
            paymentRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Pago con ID $id no encontrado")

        val previousStatus = existingPayment.status

        updatePaymentRequest.amount?.let { existingPayment.amount = it }
        updatePaymentRequest.method?.let { existingPayment.method = it }
        updatePaymentRequest.reference?.let { existingPayment.reference = it }
        updatePaymentRequest.status?.let { existingPayment.status = it }

        val savedPayment = paymentRepository.save(existingPayment)

        if (savedPayment.status != previousStatus) {
            when (savedPayment.status) {
                PaymentStatus.SUCCESSFUL -> membershipManagementService.activateMembership(savedPayment.membershipId)
                PaymentStatus.REJECTED -> membershipManagementService.deactivateMembership(savedPayment.membershipId)
                PaymentStatus.PENDING -> {}
            }
        }

        return toResponse(savedPayment)
    }

    @Transactional
    fun deletePayment(id: Int) {
        val payment =
            paymentRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Pago con ID $id no encontrado")
        paymentRepository.delete(payment)
    }

    private fun toEntity(paymentRequest: PaymentRequest): Payment =
        Payment(
            membershipId = paymentRequest.membershipId!!,
            userId = paymentRequest.userId!!,
            discountId = paymentRequest.discountId,
            amount = paymentRequest.amount!!,
            method = paymentRequest.method,
            reference = paymentRequest.reference,
            status = paymentRequest.status!!,
        )

    private fun toResponse(payment: Payment): PaymentResponse =
        PaymentResponse(
            id = payment.id ?: 0,
            membershipId = payment.membershipId,
            userId = payment.userId,
            discountId = payment.discountId,
            amount = payment.amount,
            method = payment.method,
            reference = payment.reference,
            status = payment.status,
            createdAt = payment.createdAt,
        )
}
