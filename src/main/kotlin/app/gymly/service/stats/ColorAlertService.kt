package app.gymly.service.stats

import app.gymly.dto.ColorAlertDTO
import app.gymly.model.MembershipStatus
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

    fun checkAccessColor(document: String): ColorAlertDTO {
        val user = userRepository.findByDocument(document)
            ?: return ColorAlertDTO(document, null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership = getLatestMembership(user.id!!)
            ?: return ColorAlertDTO(document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.")

        return evaluateMembershipStatus(document, user, latestMembership)
    }

    private fun getLatestMembership(userId: Int): Membership? {
        return membershipRepository.findAll()
            .filter { it.userId == userId }
            .maxByOrNull { it.endDate }
    }

    private fun evaluateMembershipStatus(document: String, user: User, membership: Membership): ColorAlertDTO {
        val fullName = user.fullName()

        if (membership.status != MembershipStatus.active) {
            return ColorAlertDTO(document, fullName, "RED", 0, "Membresía inactiva (Estado: ${membership.status}).")
        }

        val today = LocalDate.now()
        val endDate = membership.endDate

        if (endDate.isBefore(today)) {
            return ColorAlertDTO(document, fullName, "RED", 0, "Membresía vencida el $endDate.")
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, endDate)
        return buildAlertByDays(document, fullName, daysRemaining)
    }

    private fun buildAlertByDays(document: String, fullName: String, daysRemaining: Long): ColorAlertDTO {
        return when {
            daysRemaining > 7 -> {
                ColorAlertDTO(document, fullName, "GREEN", daysRemaining, "Acceso permitido. Vigente por $daysRemaining días.")
            }
            else -> {
                ColorAlertDTO(document, fullName, "YELLOW", daysRemaining, "¡Atención! Quedan $daysRemaining días o menos. Recordar cobrar renovación.")
            }
        }
    }

    private fun User.fullName() = "${this.name} ${this.lastname}"
}