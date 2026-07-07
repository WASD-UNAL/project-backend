package app.gymly.service.payment

import app.gymly.dto.payment.WebhookRequest
import org.springframework.stereotype.Service

@Service
class WebhookService(
    private val paymentConfirmationService: PaymentConfirmationService,
) {
    fun processNotification(webhookRequest: WebhookRequest) {
        if (webhookRequest.type != "payment" || webhookRequest.data?.id == null) {
            return
        }

        paymentConfirmationService.applyMercadoPagoResult(webhookRequest.data.id.toLong())
    }
}
