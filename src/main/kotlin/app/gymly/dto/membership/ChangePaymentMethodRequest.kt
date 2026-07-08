package app.gymly.dto.membership

import app.gymly.model.PaymentMethod
import jakarta.validation.constraints.NotNull

data class ChangePaymentMethodRequest(
    @field:NotNull(message = "paymentMethod is required")
    val paymentMethod: PaymentMethod?,
)
