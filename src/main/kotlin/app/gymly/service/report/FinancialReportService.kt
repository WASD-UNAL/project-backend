package app.gymly.service.report

import app.gymly.dto.report.MonthlyRevenueResponse
import app.gymly.dto.report.PeriodSummary
import app.gymly.dto.report.PlanRevenue
import app.gymly.dto.report.RevenueByPlanResponse
import app.gymly.dto.report.RevenueComparisonResponse
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Month
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Service
@Transactional(readOnly = true)
class FinancialReportService(
    private val paymentRepository: PaymentRepository,
) {
    fun getMonthlyRevenue(year: Int, month: Int): MonthlyRevenueResponse {
        val (start, end) = periodBounds(year, month)
        val totalRevenue = paymentRepository.sumAmountByCreatedAtBetweenAndStatus(start, end, PaymentStatus.SUCCESSFUL)
        val totalPayments = paymentRepository.countByCreatedAtBetweenAndStatus(start, end, PaymentStatus.SUCCESSFUL)
        return MonthlyRevenueResponse(
            year = year,
            month = month,
            label = "${Month.of(month).name} $year",
            totalRevenue = totalRevenue,
            totalPayments = totalPayments,
        )
    }

    fun getRevenueByPlan(year: Int, month: Int): RevenueByPlanResponse {
        val (start, end) = periodBounds(year, month)
        val totalRevenue = paymentRepository.sumAmountByCreatedAtBetweenAndStatus(start, end, PaymentStatus.SUCCESSFUL)
        val grouped = paymentRepository.sumAmountGroupedByPlanInPeriod(start, end, PaymentStatus.SUCCESSFUL)
        val details = grouped.map { row ->
            PlanRevenue(
                planId = (row[0] as Number).toInt(),
                planName = row[1] as String,
                revenue = row[2] as BigDecimal,
                paymentCount = (row[3] as Number).toLong(),
            )
        }
        return RevenueByPlanResponse(
            year = year,
            month = month,
            totalRevenue = totalRevenue,
            details = details,
        )
    }

    fun comparePeriods(
        currentYear: Int,
        currentMonth: Int,
        previousYear: Int,
        previousMonth: Int,
    ): RevenueComparisonResponse {
        val currentSummary = buildPeriodSummary(currentYear, currentMonth)
        val previousSummary = buildPeriodSummary(previousYear, previousMonth)

        val percentageChange = if (previousSummary.totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal.ZERO
        } else {
            currentSummary.totalRevenue
                .subtract(previousSummary.totalRevenue)
                .multiply(BigDecimal(100))
                .divide(previousSummary.totalRevenue, 2, RoundingMode.HALF_UP)
        }

        return RevenueComparisonResponse(
            currentPeriod = currentSummary,
            previousPeriod = previousSummary,
            percentageChange = percentageChange,
        )
    }

    private fun buildPeriodSummary(year: Int, month: Int): PeriodSummary {
        val (start, end) = periodBounds(year, month)
        val totalRevenue = paymentRepository.sumAmountByCreatedAtBetweenAndStatus(start, end, PaymentStatus.SUCCESSFUL)
        val totalPayments = paymentRepository.countByCreatedAtBetweenAndStatus(start, end, PaymentStatus.SUCCESSFUL)
        return PeriodSummary(
            label = "${Month.of(month).name} $year",
            totalRevenue = totalRevenue,
            totalPayments = totalPayments,
        )
    }

    private fun periodBounds(year: Int, month: Int): Pair<OffsetDateTime, OffsetDateTime> {
        val start = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now()))
        val end = start.with(TemporalAdjusters.lastDayOfMonth()).with(java.time.LocalTime.MAX)
        return Pair(start, end)
    }
}
