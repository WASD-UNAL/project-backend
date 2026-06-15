package app.gymly.controller.stats

import app.gymly.dto.stats.AttendanceStatsResponse
import app.gymly.model.StatPeriod
import app.gymly.service.stats.AttendanceStatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stats/attendance")
class AttendanceStatsController(
    private val attendanceStatsService: AttendanceStatsService,
) {
    @GetMapping
    fun getMetrics(
        @RequestParam(defaultValue = "HOURS_DAY") period: StatPeriod,
        @RequestParam(defaultValue = "0") offset: Int,
    ): ResponseEntity<AttendanceStatsResponse> {
        val stats = attendanceStatsService.calculateAttendanceMetrics(period, offset)
        return ResponseEntity.ok(stats)
    }
}
