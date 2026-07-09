package app.gymly.controller.payment

import app.gymly.dto.payment.NotificationData
import app.gymly.dto.payment.WebhookRequest
import app.gymly.service.payment.WebhookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments/webhook")
class WebhookController(
    private val webhookService: WebhookService,
) {
    @PostMapping
    fun handleMercadoPagoNotification(
        @RequestBody(required = false) body: WebhookRequest?,
        @RequestParam(name = "type", required = false) type: String?,
        @RequestParam(name = "topic", required = false) topic: String?,
        @RequestParam(name = "id", required = false) id: String?,
        @RequestParam(name = "data.id", required = false) dataId: String?,
    ): ResponseEntity<Unit> {
        val request =
            body ?: WebhookRequest(
                action = null,
                type = type ?: topic,
                data = NotificationData(dataId ?: id),
            )
        webhookService.processNotification(request)
        return ResponseEntity.ok().build()
    }
}
