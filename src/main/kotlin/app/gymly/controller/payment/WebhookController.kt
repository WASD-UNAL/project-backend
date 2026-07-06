package app.gymly.controller.payment

import app.gymly.dto.payment.WebhookRequest
import app.gymly.service.payment.WebhookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments/webhook")
class WebhookController(
    private val webhookService: WebhookService,
) {
    @PostMapping
    fun handleMercadoPagoNotification(
        @RequestBody webhookRequest: WebhookRequest,
    ): ResponseEntity<Unit> {
        webhookService.processNotification(webhookRequest)
        return ResponseEntity.ok().build()
    }
}
