package app.gymly.dto.membership

import app.gymly.model.PaymentMethod
import jakarta.validation.constraints.NotNull

data class EnrollRequest(
    @field:NotNull(message = "planId is required")
    val planId: Int?,
    @field:NotNull(message = "paymentMethod is required")
    val paymentMethod: PaymentMethod?,
)
