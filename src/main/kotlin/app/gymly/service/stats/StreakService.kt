package app.gymly.service.stats

import app.gymly.dto.client.AttendanceItem
import app.gymly.dto.client.StreakInfo
import app.gymly.dto.client.StreakPeriod
import app.gymly.repository.AttendanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class StreakService(
    private val attendanceRepository: AttendanceRepository,
) {
    fun getLastAttendances(
        userId: Int,
        limit: Int,
    ): List<AttendanceItem> =
        attendanceRepository
            .findByUserIdOrderByDateDesc(userId, PageRequest.of(0, limit))
            .mapNotNull { attendance ->
                attendance.date?.let { offsetDateTime ->
                    AttendanceItem(
                        date = offsetDateTime.toLocalDate(),
                        time = offsetDateTime.toLocalTime().toString().take(5),
                    )
                }
            }

    fun calculateStreak(userId: Int): StreakInfo {
        val dates =
            attendanceRepository
                .findByUserId(userId)
                .mapNotNull { it.date?.toLocalDate() }
                .distinct()
                .sorted()

        if (dates.isEmpty()) {
            return StreakInfo(
                currentStreak = 0,
                longestStreak = 0,
                lastAttendanceDate = null,
                streakHistory = emptyList(),
            )
        }

        val periods = buildStreakPeriods(dates)

        val current = periods.lastOrNull()
        val longest = periods.maxByOrNull { ChronoUnit.DAYS.between(it.startDate, it.endDate) }

        return StreakInfo(
            currentStreak = if (current != null) (ChronoUnit.DAYS.between(current.startDate, current.endDate) + 1).toInt() else 0,
            longestStreak = if (longest != null) (ChronoUnit.DAYS.between(longest.startDate, longest.endDate) + 1).toInt() else 0,
            lastAttendanceDate = dates.last(),
            streakHistory = periods,
        )
    }

    private fun buildStreakPeriods(dates: List<LocalDate>): List<StreakPeriod> {
        val periods = mutableListOf<StreakPeriod>()
        var start = dates.first()
        var prev = start
        var count = 1

        for (i in 1 until dates.size) {
            val current = dates[i]
            val diff = ChronoUnit.DAYS.between(prev, current)

            if (diff <= 3) {
                count++
                prev = current
            } else {
                periods.add(StreakPeriod(start, prev, count))
                start = current
                prev = current
                count = 1
            }
        }

        periods.add(StreakPeriod(start, prev, count))
        return periods
    }
}
