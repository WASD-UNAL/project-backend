package app.gymly.dto.membership

import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import java.math.BigDecimal
import java.time.OffsetDateTime

data class PaymentHistoryItemResponse(
    val id: Int?,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val reference: String?,
    val createdAt: OffsetDateTime?,
)
