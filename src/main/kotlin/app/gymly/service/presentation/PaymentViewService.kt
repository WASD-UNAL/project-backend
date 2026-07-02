package app.gymly.service.presentation

import app.gymly.dto.membership.PaymentHistoryItemResponse
import app.gymly.model.Payment
import app.gymly.repository.PaymentRepository
import org.springframework.stereotype.Service

@Service
class PaymentViewService(
    private val paymentRepository: PaymentRepository,
) {
    fun getMyPayments(userId: Int): List<PaymentHistoryItemResponse> =
        paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).map { toResponseDTO(it) }

    private fun toResponseDTO(payment: Payment): PaymentHistoryItemResponse =
        PaymentHistoryItemResponse(
            id = payment.id,
            amount = payment.amount,
            method = payment.method,
            status = payment.status,
            reference = payment.reference,
            createdAt = payment.createdAt,
        )
}
