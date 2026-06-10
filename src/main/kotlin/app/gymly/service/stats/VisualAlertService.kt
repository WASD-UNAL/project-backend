package app.gymly.service.stats

import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.model.User
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class VisualAlertService(
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository
) {

    fun checkAccessColor(document: String): VisualAlertResponse {
        val user = userRepository.findByDocument(document)
            ?: return VisualAlertResponse(document, null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership = getLatestMembership(user.id!!)
            ?: return VisualAlertResponse(document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.")

        return evaluateMembershipStatus(document, user, latestMembership)
    }

    private fun getLatestMembership(userId: Int): Membership? {
        return membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId)
    }

    private fun evaluateMembershipStatus(document: String, user: User, membership: Membership): VisualAlertResponse {
        val fullName = user.fullName()

        if (membership.status != MembershipStatus.ACTIVE) {
            return VisualAlertResponse(document, fullName, "RED", 0, "Membresía inactiva (Estado: ${membership.status}).")
        }

        val today = LocalDate.now()
        val endDate = membership.endDate

        if (endDate.isBefore(today)) {
            return VisualAlertResponse(document, fullName, "RED", 0, "Membresía vencida el $endDate.")
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, endDate)
        return buildAlertByDays(document, fullName, daysRemaining)
    }

    private fun buildAlertByDays(document: String, fullName: String, daysRemaining: Long): VisualAlertResponse {
        return when {
            daysRemaining > 7 -> {
                VisualAlertResponse(document, fullName, "GREEN", daysRemaining, "Acceso permitido. Vigente por $daysRemaining días.")
            }
            else -> {
                VisualAlertResponse(document, fullName, "YELLOW", daysRemaining, "¡Atención! Quedan $daysRemaining días o menos. Recordar cobrar renovación.")
            }
        }
    }

    private fun User.fullName() = "${this.name} ${this.lastname}"
}