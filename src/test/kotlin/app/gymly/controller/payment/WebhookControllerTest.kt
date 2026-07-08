package app.gymly.controller.payment

import app.gymly.dto.payment.WebhookRequest
import app.gymly.service.payment.WebhookService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class WebhookControllerTest {
    private lateinit var webhookService: WebhookService
    private lateinit var controller: WebhookController

    @BeforeEach
    fun setUp() {
        webhookService = mock()
        controller = WebhookController(webhookService)
    }

    @Test
    fun usesTheJsonBodyWhenPresent() {
        val body =
            WebhookRequest(
                action = "payment.created",
                type = "payment",
                data =
                    app.gymly.dto.payment
                        .NotificationData("123"),
            )

        controller.handleMercadoPagoNotification(body, null, null, null, null)

        verify(webhookService).processNotification(body)
    }

    @Test
    fun fallsBackToIpnQueryParams() {
        controller.handleMercadoPagoNotification(null, null, "payment", "999", null)

        val captor = argumentCaptor<WebhookRequest>()
        verify(webhookService).processNotification(captor.capture())
        assertEquals("payment", captor.firstValue.type)
        assertEquals("999", captor.firstValue.data?.id)
    }

    @Test
    fun readsTheModernDataIdQueryParam() {
        controller.handleMercadoPagoNotification(null, "payment", null, null, "555")

        val captor = argumentCaptor<WebhookRequest>()
        verify(webhookService).processNotification(captor.capture())
        assertEquals("payment", captor.firstValue.type)
        assertEquals("555", captor.firstValue.data?.id)
    }
}
