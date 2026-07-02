package app.gymly.dto.payment

data class CheckoutResponse(
    val paymentId: Int,
    val checkoutUrl: String,
)
