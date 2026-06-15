package app.gymly.service.stats

import app.gymly.model.Attendance
import app.gymly.model.StatPeriod
import app.gymly.repository.AttendanceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class AttendanceStatsServiceTest {
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var attendanceStatsService: AttendanceStatsService

    @BeforeEach
    fun setUp() {
        attendanceRepository = mock()
        attendanceStatsService = AttendanceStatsService(attendanceRepository)
    }

    private fun makeAttendance(
        dateTime: LocalDateTime,
        userId: Int = 1,
    ): Attendance {
        val offset = ZoneId.systemDefault().rules.getOffset(dateTime)
        return Attendance(userId = userId, date = dateTime.atOffset(offset))
    }

    @Test
    fun `groups and counts attendances by hour correctly`() {
        val today = LocalDate.now()
        whenever(attendanceRepository.findByDateBetween(any(), any())).thenReturn(
            listOf(
                makeAttendance(today.atTime(9, 0)),
                makeAttendance(today.atTime(9, 30)),
                makeAttendance(today.atTime(9, 59)),
                makeAttendance(today.atTime(14, 0)),
                makeAttendance(today.atTime(14, 45)),
            ),
        )

        val response = attendanceStatsService.calculateAttendanceMetrics(StatPeriod.HOURS_DAY, 0)

        assertEquals(18, response.points.size)
        assertEquals("06:00", response.points.first().label)
        assertEquals("23:00", response.points.last().label)
        assertEquals(3L, response.points.first { it.label == "09:00" }.count)
        assertEquals(2L, response.points.first { it.label == "14:00" }.count)
        assertEquals(0L, response.points.first { it.label == "10:00" }.count)
        assertEquals(0L, response.points.first { it.label == "22:00" }.count)
    }

    @Test
    fun `peak value matches the highest attendance count across all points`() {
        val today = LocalDate.now()
        whenever(attendanceRepository.findByDateBetween(any(), any())).thenReturn(
            listOf(
                makeAttendance(today.atTime(10, 0)),
                makeAttendance(today.atTime(10, 15)),
                makeAttendance(today.atTime(10, 45)),
                makeAttendance(today.atTime(15, 0)),
            ),
        )

        val response = attendanceStatsService.calculateAttendanceMetrics(StatPeriod.HOURS_DAY, 0)

        assertEquals(3L, response.peakValue)
        assertEquals(response.points.maxOf { it.count }, response.peakValue)

        whenever(attendanceRepository.findByDateBetween(any(), any())).thenReturn(emptyList())

        val emptyResponse = attendanceStatsService.calculateAttendanceMetrics(StatPeriod.HOURS_DAY, 0)

        assertEquals(1L, emptyResponse.peakValue)
        assertTrue(emptyResponse.points.all { it.count == 0L })
    }

    @Test
    fun `ignores attendances with null date without throwing exception`() {
        whenever(attendanceRepository.findByDateBetween(any(), any())).thenReturn(
            listOf(
                Attendance(userId = 1, date = null),
                Attendance(userId = 2, date = null),
                makeAttendance(LocalDate.now().atTime(10, 0), userId = 3),
            ),
        )

        val response = attendanceStatsService.calculateAttendanceMetrics(StatPeriod.HOURS_DAY, 0)

        assertEquals(1L, response.points.first { it.label == "10:00" }.count)
        assertEquals(0L, response.points.filter { it.label != "10:00" }.sumOf { it.count })
        assertEquals(1L, response.peakValue)
    }
}
