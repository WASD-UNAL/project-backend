package app.gymly.dto.membership

import java.math.BigDecimal

data class PlanResponse(
    val id: Int?,
    val name: String,
    val description: String?,
    val durationDays: Int,
    val price: BigDecimal,
    val active: Boolean,
    val discount: PlanDiscountResponse? = null,
)
