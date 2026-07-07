package app.gymly.service.stats

import app.gymly.constants.AppConstants
import app.gymly.dto.stats.AttendanceStatsResponse
import app.gymly.dto.stats.StatPoint
import app.gymly.model.StatPeriod
import app.gymly.repository.AttendanceRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.temporal.TemporalAdjusters

@Service
class AttendanceStatsService(
    private val attendanceRepository: AttendanceRepository,
) {
    fun calculateAttendanceMetrics(
        period: StatPeriod,
        offset: Int,
    ): AttendanceStatsResponse {
        val targetDate =
            when (period) {
                StatPeriod.HOURS_DAY -> LocalDate.now().plusDays(offset.toLong())
                StatPeriod.DAYS_WEEK -> LocalDate.now().plusWeeks(offset.toLong())
                StatPeriod.DAYS_MONTH -> LocalDate.now().plusMonths(offset.toLong())
                StatPeriod.MONTHS_YEAR -> LocalDate.now().plusYears(offset.toLong())
            }

        val points =
            when (period) {
                StatPeriod.HOURS_DAY -> calculateHoursDay(targetDate)
                StatPeriod.DAYS_WEEK -> calculateDaysWeek(targetDate)
                StatPeriod.DAYS_MONTH -> calculateDaysMonth(targetDate)
                StatPeriod.MONTHS_YEAR -> calculateMonthsYear(targetDate)
            }

        val peakValue = (points.maxOfOrNull { it.count } ?: 0L).coerceAtLeast(1L)

        return AttendanceStatsResponse(
            period = period,
            peakValue = peakValue,
            points = points,
        )
    }

    private fun calculateHoursDay(date: LocalDate): List<StatPoint> {
        val startOfDay = date.atStartOfDay().atZone(AppConstants.APP_ZONE_ID).toOffsetDateTime()
        val endOfDay = date.atTime(LocalTime.MAX).atZone(AppConstants.APP_ZONE_ID).toOffsetDateTime()
        val attendances = attendanceRepository.findByDateBetween(startOfDay, endOfDay)

        val groupedByHour =
            attendances
                .filter { it.date != null }
                .groupBy { it.date!!.atZoneSameInstant(AppConstants.APP_ZONE_ID).hour }

        val startHour = 6
        val endHour = 23
        val operatingHours = startHour..endHour

        return operatingHours.map { hour ->
            val count = groupedByHour[hour]?.size?.toLong() ?: 0L
            StatPoint(
                label = "${String.format("%02d", hour)}:00",
                count = count,
            )
        }
    }

    private fun calculateDaysWeek(date: LocalDate): List<StatPoint> {
        val startOfWeek =
            date
                .with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY),
                ).atStartOfDay()
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val endOfWeek =
            startOfWeek
                .toLocalDate()
                .plusDays(6)
                .atTime(LocalTime.MAX)
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val attendances = attendanceRepository.findByDateBetween(startOfWeek, endOfWeek)

        val groupedByDay =
            attendances
                .filter { it.date != null }
                .groupBy { it.date!!.atZoneSameInstant(AppConstants.APP_ZONE_ID).dayOfWeek }

        return DayOfWeek.entries.map { dayOfWeek ->
            val count = groupedByDay[dayOfWeek]?.size?.toLong() ?: 0L
            StatPoint(
                label = dayOfWeek.name,
                count = count,
            )
        }
    }

    private fun calculateDaysMonth(date: LocalDate): List<StatPoint> {
        val startOfMonth =
            date
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay()
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val endOfMonth =
            date
                .with(
                    TemporalAdjusters.lastDayOfMonth(),
                ).atTime(LocalTime.MAX)
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val attendances = attendanceRepository.findByDateBetween(startOfMonth, endOfMonth)

        val groupedByDayOfMonth =
            attendances
                .filter { it.date != null }
                .groupBy { it.date!!.atZoneSameInstant(AppConstants.APP_ZONE_ID).dayOfMonth }

        val daysInMonth = date.lengthOfMonth()

        return (1..daysInMonth).map { day ->
            val count = groupedByDayOfMonth[day]?.size?.toLong() ?: 0L
            StatPoint(
                label = String.format("%02d", day),
                count = count,
            )
        }
    }

    private fun calculateMonthsYear(date: LocalDate): List<StatPoint> {
        val startOfYear =
            date
                .with(TemporalAdjusters.firstDayOfYear())
                .atStartOfDay()
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val endOfYear =
            date
                .with(TemporalAdjusters.lastDayOfYear())
                .atTime(LocalTime.MAX)
                .atZone(AppConstants.APP_ZONE_ID)
                .toOffsetDateTime()
        val attendances = attendanceRepository.findByDateBetween(startOfYear, endOfYear)

        val groupedByMonth =
            attendances
                .filter { it.date != null }
                .groupBy { it.date!!.atZoneSameInstant(AppConstants.APP_ZONE_ID).month }

        return Month.entries.map { month ->
            val count = groupedByMonth[month]?.size?.toLong() ?: 0L
            StatPoint(
                label = month.name,
                count = count,
            )
        }
    }
}
