package app.gymly.service.stats

import app.gymly.model.Attendance
import app.gymly.repository.AttendanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StreakServiceTest {
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var streakService: StreakService

    @BeforeEach
    fun setUp() {
        attendanceRepository = mock()
        streakService = StreakService(attendanceRepository)
    }

    private fun attendanceAt(
        date: LocalDate,
        userId: Int = 1,
    ): Attendance {
        val ldt = date.atStartOfDay()
        val offset = ZoneId.systemDefault().rules.getOffset(ldt)
        return Attendance(userId = userId, date = ldt.atOffset(offset))
    }

    @Nested
    inner class NoAttendances {
        @Test
        fun returnsZeroForAllFields() {
            whenever(attendanceRepository.findByUserId(1)).thenReturn(emptyList())

            val result = streakService.calculateStreak(1)

            assertEquals(0, result.currentStreak)
            assertEquals(0, result.longestStreak)
            assertEquals(null, result.lastAttendanceDate)
            assertEquals(emptyList<Any>(), result.streakHistory)
        }
    }

    @Nested
    inner class SingleAttendance {
        @Test
        fun returnsStreakOfOne() {
            val today = LocalDate.now()
            whenever(attendanceRepository.findByUserId(1)).thenReturn(listOf(attendanceAt(today)))

            val result = streakService.calculateStreak(1)

            assertEquals(1, result.currentStreak)
            assertEquals(1, result.longestStreak)
            assertEquals(today, result.lastAttendanceDate)
            assertEquals(1, result.streakHistory.size)
        }
    }

    @Nested
    inner class ConsecutiveDays {
        @Test
        fun groupsConsecutiveDaysIntoSingleStreak() {
            val dates =
                listOf(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 2),
                    LocalDate.of(2025, 6, 3),
                    LocalDate.of(2025, 6, 4),
                )
            whenever(attendanceRepository.findByUserId(1)).thenReturn(dates.map { attendanceAt(it) })

            val result = streakService.calculateStreak(1)

            assertEquals(4, result.currentStreak)
            assertEquals(4, result.longestStreak)
            assertEquals(dates.last(), result.lastAttendanceDate)
            assertEquals(1, result.streakHistory.size)
            val period = result.streakHistory.first()
            assertEquals(dates.first(), period.startDate)
            assertEquals(dates.last(), period.endDate)
            assertEquals(4, period.attendanceDays)
        }
    }

    @Nested
    inner class WithRestDays {
        @Test
        fun allowsUpToTwoRestDaysWithinSameStreak() {
            val dates =
                listOf(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 4),
                )
            whenever(attendanceRepository.findByUserId(1)).thenReturn(dates.map { attendanceAt(it) })

            val result = streakService.calculateStreak(1)

            assertEquals(4, result.currentStreak)
            assertEquals(1, result.streakHistory.size)
        }

        @Test
        fun breaksStreakAfterThreeDaysGap() {
            val dates =
                listOf(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 5),
                )
            whenever(attendanceRepository.findByUserId(1)).thenReturn(dates.map { attendanceAt(it) })

            val result = streakService.calculateStreak(1)

            assertEquals(1, result.currentStreak)
            assertEquals(1, result.longestStreak)
            assertEquals(2, result.streakHistory.size)
        }
    }

    @Nested
    inner class MultipleStreaks {
        @Test
        fun identifiesLongestAndCurrentStreaks() {
            val dates =
                listOf(
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 5, 2),
                    LocalDate.of(2025, 5, 10),
                    LocalDate.of(2025, 5, 11),
                    LocalDate.of(2025, 5, 12),
                    LocalDate.of(2025, 5, 13),
                    LocalDate.of(2025, 6, 1),
                )
            whenever(attendanceRepository.findByUserId(1)).thenReturn(dates.map { attendanceAt(it) })

            val result = streakService.calculateStreak(1)

            assertEquals(1, result.currentStreak)
            assertEquals(4, result.longestStreak)
            assertEquals(3, result.streakHistory.size)
        }
    }

    @Nested
    inner class DuplicateDates {
        @Test
        fun ignoresDuplicateAttendanceDates() {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val attendances =
                listOf(
                    attendanceAt(yesterday),
                    attendanceAt(today),
                    Attendance(userId = 1, date = today.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()),
                )
            whenever(attendanceRepository.findByUserId(1)).thenReturn(attendances)

            val result = streakService.calculateStreak(1)

            assertEquals(2, result.currentStreak)
            assertEquals(1, result.streakHistory.size)
        }
    }
}
