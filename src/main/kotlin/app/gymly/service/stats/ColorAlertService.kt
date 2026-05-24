package app.gymly.service.stats

import app.gymly.dto.ColorAlert
import app.gymly.model.User
import app.gymly.model.Membership
import app.gymly.repository.MembershipRepository
import app.gymly.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class ColorAlertService(
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository
) {

    fun checkAccessColor(document: Int): ColorAlert {
        val user = userRepository.findByDocument(document)
            ?: return ColorAlert(document, null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership = getLatestMembership(user.id)
            ?: return ColorAlert(document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.")

        return evaluateMembershipStatus(document, user, latestMembership)
    }

    private fun getLatestMembership(userId: Int): Membership? {
        return membershipRepository.findAll()
            .filter { it.userId == userId }
            .maxByOrNull { it.endDate }
    }

    private fun evaluateMembershipStatus(document: Int, user: User, membership: Membership): ColorAlert {
        val fullName = user.fullName()

        if (membership.status != "active") {
            return ColorAlert(document, fullName, "RED", 0, "Membresía inactiva (Estado: ${membership.status}).")
        }

        val today = LocalDate.now()
        val endDate = membership.endDate

        if (endDate.isBefore(today)) {
            return ColorAlert(document, fullName, "RED", 0, "Membresía vencida el $endDate.")
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, endDate)
        return buildAlertByDays(document, fullName, daysRemaining)
    }

    private fun buildAlertByDays(document: Int, fullName: String, daysRemaining: Long): ColorAlert {
        return when {
            daysRemaining > 7 -> {
                ColorAlert(document, fullName, "GREEN", daysRemaining, "Acceso permitido. Vigente por $daysRemaining días.")
            }
            else -> {
                ColorAlert(document, fullName, "YELLOW", daysRemaining, "¡Atención! Quedan $daysRemaining días o menos. Recordar cobrar renovación.")
            }
        }
    }

    private fun User.fullName() = "${this.name} ${this.lastname}"
}