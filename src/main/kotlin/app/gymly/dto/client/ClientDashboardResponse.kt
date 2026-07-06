package app.gymly.dto.client

import app.gymly.model.MembershipStatus
import java.math.BigDecimal
import java.time.LocalDate

data class ClientDashboardResponse(
    val user: ClientInfo,
    val memberships: List<MembershipHistoryItem>,
    val recentAttendances: List<AttendanceItem>,
    val streak: StreakInfo,
)

data class ClientInfo(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val document: String,
    val phone: String?,
    val weight: BigDecimal?,
    val height: BigDecimal?,
)

data class MembershipHistoryItem(
    val id: Int,
    val planName: String,
    val initDate: LocalDate,
    val endDate: LocalDate,
    val status: MembershipStatus,
    val daysRemaining: Long?,
)

data class AttendanceItem(
    val date: LocalDate,
    val time: String?,
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastAttendanceDate: LocalDate?,
    val streakHistory: List<StreakPeriod>,
)

data class StreakPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendanceDays: Int,
)
