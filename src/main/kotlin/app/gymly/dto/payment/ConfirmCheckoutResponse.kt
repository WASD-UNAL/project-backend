package app.gymly.dto.payment

import app.gymly.model.PaymentStatus

data class ConfirmCheckoutResponse(
    val paymentId: Int,
    val status: PaymentStatus,
)
