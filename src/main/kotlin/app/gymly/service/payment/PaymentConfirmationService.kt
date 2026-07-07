package app.gymly.service.payment

import app.gymly.model.Payment
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import app.gymly.service.membership.MembershipManagementService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

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

        val localPaymentIdStr = mpPayment.externalReference
        if (localPaymentIdStr.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago de Mercado Pago no contiene external_reference")
        }

        val localPaymentId =
            localPaymentIdStr.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El external_reference del pago no es válido")

        val localPayment =
            paymentRepository.findByIdOrNull(localPaymentId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Pago local con ID $localPaymentId no encontrado")

        if (requesterUserId != null && localPayment.userId != requesterUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "El pago no pertenece al usuario autenticado")
        }

        if (localPayment.status == PaymentStatus.SUCCESSFUL) {
            return localPayment
        }

        when (mpPayment.status) {
            "approved" -> {
                localPayment.status = PaymentStatus.SUCCESSFUL
                if (localPayment.reference.isNullOrBlank()) {
                    localPayment.reference = "MP-$mpPaymentId"
                }
                paymentRepository.save(localPayment)

                membershipManagementService.activateMembership(localPayment.membershipId)
            }
            "rejected", "cancelled" -> {
                localPayment.status = PaymentStatus.REJECTED
                if (localPayment.reference.isNullOrBlank()) {
                    localPayment.reference = "MP-$mpPaymentId"
                }
                paymentRepository.save(localPayment)

                membershipManagementService.deactivateMembership(localPayment.membershipId)
            }
        }

        return localPayment
    }
}
