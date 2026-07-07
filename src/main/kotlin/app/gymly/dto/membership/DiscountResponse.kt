package app.gymly.dto.membership

import java.math.BigDecimal
import java.time.LocalDate

data class DiscountResponse(
    val id: Int?,
    val name: String,
    val description: String?,
    val percentage: BigDecimal,
    val initDate: LocalDate,
    val endDate: LocalDate,
    val active: Boolean,
    val plans: List<DiscountPlanSummary>,
)

data class DiscountPlanSummary(
    val id: Int,
    val name: String,
)
