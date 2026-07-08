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
    /**
     * Mercado Pago envía las notificaciones en dos formatos distintos según la
     * configuración: el formato moderno de Webhooks manda un cuerpo JSON
     * (`{ "type": "payment", "data": { "id": "123" } }`), mientras que el
     * formato IPN clásico manda los datos por query-params
     * (`?topic=payment&id=123` o `?type=payment&data.id=123`).
     * Aceptamos ambos para no perder confirmaciones.
     */
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
