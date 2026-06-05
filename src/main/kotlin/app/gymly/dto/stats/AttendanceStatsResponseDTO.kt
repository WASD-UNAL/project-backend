package app.gymly.dto.stats

import app.gymly.model.StatPeriod

data class AttendanceStatsResponseDTO(
    val period: StatPeriod,
    val peakValue: Long,
    val points: List<StatPointDTO>
)