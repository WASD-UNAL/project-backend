package app.gymly.dto.report

import java.math.BigDecimal

data class MonthlyRevenueResponse(
    val year: Int,
    val month: Int,
    val label: String,
    val totalRevenue: BigDecimal,
    val totalPayments: Long,
)
