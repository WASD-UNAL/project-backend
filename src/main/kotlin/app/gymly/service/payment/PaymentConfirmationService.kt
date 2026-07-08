package app.gymly.service.payment

import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import app.gymly.service.membership.MembershipManagementService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class PaymentConfirmationService(
    private val paymentRepository: PaymentRepository,
    private val membershipManagementService: MembershipManagementService,
    private val mercadoPagoService: MercadoPagoService,
) {
    @Transactional
    fun applyMercadoPagoResult(
        mpPaymentId: Long,
        requesterUserId: Int? = null,
    ): Payment {
        val mpPayment = mercadoPagoService.getPayment(mpPaymentId)
        val localPayment = resolveLocalPayment(mpPayment.externalReference, requesterUserId)

        if (localPayment.status != PaymentStatus.PENDING) {
            return localPayment
        }

        if (mpPayment.status == "approved") {
            approve(localPayment, mpPaymentId)
        }

        return localPayment
    }

    @Transactional
    fun reconcilePendingCardPayment(paymentId: Int) {
        val payment = paymentRepository.findByIdOrNull(paymentId) ?: return
        if (payment.method != PaymentMethod.CARD || payment.status != PaymentStatus.PENDING) {
            return
        }

        val reference = payment.checkoutReference ?: paymentId.toString()

        // La búsqueda de Mercado Pago puede devolver pagos que no corresponden al
        // checkout actual (referencias repetidas de otros entornos o resultados sin
        // filtrar), así que solo se consideran intentos con la referencia exacta.
        val attempts =
            mercadoPagoService
                .searchPaymentsByExternalReference(reference)
                .filter { it.externalReference == reference }

        val approved = attempts.firstOrNull { it.status == "approved" }
        if (approved != null) {
            approve(payment, approved.id)
            return
        }

        val createdAt = payment.createdAt ?: return
        if (createdAt.isBefore(OffsetDateTime.now().minusMinutes(RETRY_GRACE_MINUTES))) {
            reject(payment, attempts.firstOrNull()?.id)
        }
    }

    private fun approve(
        payment: Payment,
        mpPaymentId: Long?,
    ) {
        payment.status = PaymentStatus.SUCCESSFUL
        if (payment.reference.isNullOrBlank() && mpPaymentId != null) {
            payment.reference = "MP-$mpPaymentId"
        }
        paymentRepository.save(payment)
        membershipManagementService.activateMembership(payment.membershipId)
    }

    private fun reject(
        payment: Payment,
        mpPaymentId: Long?,
    ) {
        payment.status = PaymentStatus.REJECTED
        if (payment.reference.isNullOrBlank() && mpPaymentId != null) {
            payment.reference = "MP-$mpPaymentId"
        }
        paymentRepository.save(payment)
        membershipManagementService.deactivateMembership(payment.membershipId)
    }

    private fun resolveLocalPayment(
        externalReference: String?,
        requesterUserId: Int?,
    ): Payment {
        if (externalReference.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago de Mercado Pago no contiene external_reference")
        }

        val localPayment =
            paymentRepository.findByCheckoutReference(externalReference)
                ?: resolveLegacyPayment(externalReference)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ningún pago local corresponde al external_reference $externalReference",
                )

        if (requesterUserId != null && localPayment.userId != requesterUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "El pago no pertenece al usuario autenticado")
        }

        return localPayment
    }

    /**
     * Los checkouts creados antes de introducir checkout_reference usaban el ID local
     * del pago como external_reference. Solo se acepta ese formato si el pago aún no
     * tiene una referencia propia: si ya la tiene, un external_reference numérico
     * proviene de un pago viejo de la cuenta de Mercado Pago y no debe confundirse
     * con el checkout actual.
     */
    private fun resolveLegacyPayment(externalReference: String): Payment? {
        val localPaymentId = externalReference.toIntOrNull() ?: return null
        val payment = paymentRepository.findByIdOrNull(localPaymentId) ?: return null
        return payment.takeIf { it.checkoutReference == null }
    }

    private companion object {
        const val RETRY_GRACE_MINUTES = 3L
    }
}
