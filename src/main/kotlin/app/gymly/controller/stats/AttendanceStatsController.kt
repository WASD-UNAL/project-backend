package app.gymly.controller.stats

import app.gymly.dto.stats.AttendanceStatsResponseDTO
import app.gymly.model.StatPeriod
import app.gymly.service.stats.AttendanceStatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stats/attendance")
class AttendanceStatsController(private val attendanceStatsService: AttendanceStatsService) {

    @GetMapping
    fun getMetrics(
        @RequestParam(defaultValue = "HOURS_DAY") period: StatPeriod,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<AttendanceStatsResponseDTO> {
        val stats = attendanceStatsService.calculateMetrics(period, offset)
        return ResponseEntity.ok(stats)
    }
}