package app.gymly.dto.report

import java.math.BigDecimal

data class RevenueComparisonResponse(
    val currentPeriod: PeriodSummary,
    val previousPeriod: PeriodSummary,
    val percentageChange: BigDecimal,
)

data class PeriodSummary(
    val label: String,
    val totalRevenue: BigDecimal,
    val totalPayments: Long,
)
