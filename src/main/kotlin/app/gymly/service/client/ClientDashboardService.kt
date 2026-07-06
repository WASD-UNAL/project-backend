package app.gymly.service.client

import app.gymly.dto.client.AttendanceItem
import app.gymly.dto.client.ClientDashboardResponse
import app.gymly.dto.client.ClientInfo
import app.gymly.dto.client.MembershipHistoryItem
import app.gymly.model.MembershipStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PlanRepository
import app.gymly.repository.UserRepository
import app.gymly.service.stats.StreakService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class ClientDashboardService(
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository,
    private val planRepository: PlanRepository,
    private val streakService: StreakService,
) {
    @Transactional(readOnly = true)
    fun getDashboard(userId: Int): ClientDashboardResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado") }

        val memberships = membershipRepository.findByUserId(userId)
        val today = LocalDate.now()

        val membershipHistory = memberships.map { membership ->
            val plan = planRepository.findByIdOrNull(membership.planId)
            val daysRemaining = if (membership.status == MembershipStatus.ACTIVE) {
                ChronoUnit.DAYS.between(today, membership.endDate).coerceAtLeast(0)
            } else null

            MembershipHistoryItem(
                id = membership.id ?: 0,
                planName = plan?.name ?: "Sin plan",
                initDate = membership.initDate,
                endDate = membership.endDate,
                status = membership.status,
                daysRemaining = daysRemaining,
            )
        }.sortedByDescending { it.initDate }

        val attendances = streakService.getLastAttendances(userId, 10)

        val streak = streakService.calculateStreak(userId)

        return ClientDashboardResponse(
            user = ClientInfo(
                id = user.id ?: 0,
                name = user.name,
                lastname = user.lastname,
                email = user.email,
                document = user.document,
                phone = user.phone,
                weight = user.weight,
                height = user.height,
            ),
            memberships = membershipHistory,
            recentAttendances = attendances,
            streak = streak,
        )
    }
}
