package app.gymly.dto.report

import java.math.BigDecimal

data class RevenueByPlanResponse(
    val year: Int,
    val month: Int,
    val totalRevenue: BigDecimal,
    val details: List<PlanRevenue>,
)

data class PlanRevenue(
    val planId: Int,
    val planName: String,
    val revenue: BigDecimal,
    val paymentCount: Long,
)
