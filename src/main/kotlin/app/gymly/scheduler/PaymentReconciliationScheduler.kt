package app.gymly.scheduler

import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import app.gymly.service.payment.PaymentConfirmationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PaymentReconciliationScheduler(
    private val paymentRepository: PaymentRepository,
    private val paymentConfirmationService: PaymentConfirmationService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = RECONCILE_INTERVAL_MS)
    fun reconcilePendingCardPayments() {
        val pending = paymentRepository.findByMethodAndStatus(PaymentMethod.CARD, PaymentStatus.PENDING)
        for (payment in pending) {
            val paymentId = payment.id ?: continue
            try {
                paymentConfirmationService.reconcilePendingCardPayment(paymentId)
            } catch (e: Exception) {
                logger.warn("No se pudo reconciliar el pago con tarjeta $paymentId contra Mercado Pago", e)
            }
        }
    }

    private companion object {
        const val RECONCILE_INTERVAL_MS = 20_000L
    }
}
