package app.gymly.service.stats

import app.gymly.dto.VisualAlertDTO
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

    fun checkAccessColor(document: String): VisualAlertDTO {
        val user = userRepository.findByDocument(document)
            ?: return VisualAlertDTO(document, null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership = getLatestMembership(user.id)
            ?: return VisualAlertDTO(document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.")

        return evaluateMembershipStatus(document, user, latestMembership)
    }

    private fun getLatestMembership(userId: Int): Membership? {
        return membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId)
    }

    private fun evaluateMembershipStatus(document: String, user: User, membership: Membership): VisualAlertDTO {
        val fullName = user.fullName()

        if (membership.status != MembershipStatus.ACTIVE) {
            return VisualAlertDTO(document, fullName, "RED", 0, "Membresía inactiva (Estado: ${membership.status}).")
        }

        val today = LocalDate.now()
        val endDate = membership.endDate

        if (endDate.isBefore(today)) {
            return VisualAlertDTO(document, fullName, "RED", 0, "Membresía vencida el $endDate.")
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, endDate)
        return buildAlertByDays(document, fullName, daysRemaining)
    }

    private fun buildAlertByDays(document: String, fullName: String, daysRemaining: Long): VisualAlertDTO {
        return when {
            daysRemaining > 7 -> {
                VisualAlertDTO(document, fullName, "GREEN", daysRemaining, "Acceso permitido. Vigente por $daysRemaining días.")
            }
            else -> {
                VisualAlertDTO(document, fullName, "YELLOW", daysRemaining, "¡Atención! Quedan $daysRemaining días o menos. Recordar cobrar renovación.")
            }
        }
    }

    private fun User.fullName() = "${this.name} ${this.lastname}"
}