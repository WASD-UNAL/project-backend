package app.gymly.dto.membership

import java.math.BigDecimal
import java.time.LocalDate

data class PlanDiscountResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val percentage: BigDecimal,
    val discountedPrice: BigDecimal,
    val endDate: LocalDate,
)
