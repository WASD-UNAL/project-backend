package app.gymly.service.payment

import app.gymly.dto.payment.NotificationData
import app.gymly.dto.payment.WebhookRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class WebhookServiceTest {
    private lateinit var paymentConfirmationService: PaymentConfirmationService
    private lateinit var service: WebhookService

    @BeforeEach
    fun setUp() {
        paymentConfirmationService = mock()
        service = WebhookService(paymentConfirmationService)
    }

    @Test
    fun ignoresNotificationsThatAreNotPayments() {
        service.processNotification(WebhookRequest(action = "created", type = "merchant_order", data = NotificationData("123")))

        verify(paymentConfirmationService, never()).applyMercadoPagoResult(any(), anyOrNull())
    }

    @Test
    fun ignoresPaymentNotificationsWithoutId() {
        service.processNotification(WebhookRequest(action = "created", type = "payment", data = NotificationData(null)))

        verify(paymentConfirmationService, never()).applyMercadoPagoResult(any(), anyOrNull())
    }

    @Test
    fun delegatesPaymentNotificationsToConfirmationService() {
        service.processNotification(WebhookRequest(action = "created", type = "payment", data = NotificationData("123456789")))

        verify(paymentConfirmationService).applyMercadoPagoResult(eq(123456789L), anyOrNull())
    }
}
