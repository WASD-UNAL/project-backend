package app.gymly.service.payment

import app.gymly.dto.payment.WebhookRequest
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import app.gymly.service.membership.MembershipManagementService
import com.mercadopago.client.payment.PaymentClient
import com.mercadopago.exceptions.MPApiException
import com.mercadopago.exceptions.MPException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class WebhookService(
    private val paymentRepository: PaymentRepository,
    private val membershipManagementService: MembershipManagementService,
) {
    @Transactional
    fun processNotification(webhookRequest: WebhookRequest) {
        if (webhookRequest.type != "payment" || webhookRequest.data?.id == null) {
            return
        }

        try {
            val paymentClient = PaymentClient()
            val mpPaymentId = webhookRequest.data.id.toLong()
            val mpPayment = paymentClient.get(mpPaymentId)

            val localPaymentIdStr = mpPayment.externalReference
            if (localPaymentIdStr.isNullOrBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago de Mercado Pago no contiene external_reference")
            }

            val localPaymentId = localPaymentIdStr.toInt()

            val localPayment =
                paymentRepository.findByIdOrNull(localPaymentId)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Pago local con ID $localPaymentId no encontrado")

            if (localPayment.status == PaymentStatus.SUCCESSFUL) {
                return
            }

            when (mpPayment.status) {
                "approved" -> {
                    localPayment.status = PaymentStatus.SUCCESSFUL
                    localPayment.reference = "MP-$mpPaymentId"
                    paymentRepository.save(localPayment)

                    membershipManagementService.activateMembership(localPayment.membershipId)
                }
                "rejected", "cancelled" -> {
                    localPayment.status = PaymentStatus.REJECTED
                    localPayment.reference = "MP-$mpPaymentId"
                    paymentRepository.save(localPayment)

                    membershipManagementService.deactivateMembership(localPayment.membershipId)
                }
            }
        } catch (e: MPApiException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar en MP: ${e.apiResponse.content}", e)
        } catch (e: MPException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del SDK", e)
        }
    }
}
