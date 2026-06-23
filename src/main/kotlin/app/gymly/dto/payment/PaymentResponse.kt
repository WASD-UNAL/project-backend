package app.gymly.dto.payment

import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import java.math.BigDecimal
import java.time.OffsetDateTime

data class PaymentResponse(
    val id: Int,
    val membershipId: Int,
    val userId: Int,
    val discountId: Int?,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val reference: String?,
    val status: PaymentStatus,
    val createdAt: OffsetDateTime?
)