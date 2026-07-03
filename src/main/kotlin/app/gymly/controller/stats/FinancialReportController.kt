package app.gymly.controller.stats

import app.gymly.dto.report.MonthlyRevenueResponse
import app.gymly.dto.report.RevenueByPlanResponse
import app.gymly.dto.report.RevenueComparisonResponse
import app.gymly.service.report.FinancialReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/admin/reports")
class FinancialReportController(
    private val financialReportService: FinancialReportService,
) {
    @GetMapping("/monthly-revenue")
    fun getMonthlyRevenue(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
    ): ResponseEntity<MonthlyRevenueResponse> {
        val today = LocalDate.now()
        val y = year ?: today.year
        val m = month ?: today.monthValue
        val result = financialReportService.getMonthlyRevenue(y, m)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/revenue-by-plan")
    fun getRevenueByPlan(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
    ): ResponseEntity<RevenueByPlanResponse> {
        val today = LocalDate.now()
        val y = year ?: today.year
        val m = month ?: today.monthValue
        val result = financialReportService.getRevenueByPlan(y, m)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/revenue-comparison")
    fun comparePeriods(
        @RequestParam(required = false) currentYear: Int?,
        @RequestParam(required = false) currentMonth: Int?,
        @RequestParam(required = false) previousYear: Int?,
        @RequestParam(required = false) previousMonth: Int?,
    ): ResponseEntity<RevenueComparisonResponse> {
        val today = LocalDate.now()
        val cy = currentYear ?: today.year
        val cm = currentMonth ?: today.monthValue
        val py = previousYear ?: today.minusMonths(1).year
        val pm = previousMonth ?: today.minusMonths(1).monthValue
        val result = financialReportService.comparePeriods(cy, cm, py, pm)
        return ResponseEntity.ok(result)
    }
}
