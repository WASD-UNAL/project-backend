package app.gymly.service.stats

import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.User
import app.gymly.repository.MembershipRepository
import app.gymly.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class VisualAlertService(
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository,
) {
    fun checkAccessColor(document: String): VisualAlertResponse {
        val user =
            userRepository.findByDocument(document)
                ?: return VisualAlertResponse(document, null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership =
            getLatestMembership(user.id!!)
                ?: return VisualAlertResponse(document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.", user.id)

        return evaluateMembershipStatus(document, user, latestMembership)
    }

    /**
     * Misma evaluación de semáforo que [checkAccessColor], pero identificando
     * al usuario por su id (para endpoints self-service bajo la ruta "/me") en
     * vez de por documento. Reusa [getLatestMembership] y [evaluateMembershipStatus].
     */
    fun checkAccessColorByUserId(userId: Int): VisualAlertResponse {
        val user =
            userRepository.findById(userId).orElse(null)
                ?: return VisualAlertResponse("", null, "RED", 0, "Usuario no registrado en el sistema.")

        val latestMembership =
            getLatestMembership(userId)
                ?: return VisualAlertResponse(
                    user.document,
                    user.fullName(),
                    "RED",
                    0,
                    "El usuario no cuenta con ninguna membresía.",
                    user.id,
                )

        return evaluateMembershipStatus(user.document, user, latestMembership)
    }

    fun getCustomersByAlertStatus(targetStatus: String): List<VisualAlertResponse> {
        val allUsers = userRepository.findAll().toList()
        return allUsers
            .map { user ->
                val latestMembership = getLatestMembership(user.id ?: 0)
                if (latestMembership == null) {
                    VisualAlertResponse(user.document, user.fullName(), "RED", 0, "El usuario no cuenta con ninguna membresía.", user.id)
                } else {
                    evaluateMembershipStatus(user.document, user, latestMembership)
                }
            }.filter { it.statusColor == targetStatus }
    }

    private fun getLatestMembership(userId: Int): Membership? = membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId)

    private fun evaluateMembershipStatus(
        document: String,
        user: User,
        membership: Membership,
    ): VisualAlertResponse {
        val fullName = user.fullName()

        if (membership.status != MembershipStatus.ACTIVE) {
            return VisualAlertResponse(document, fullName, "RED", 0, "Membresía inactiva (Estado: ${membership.status}).", user.id)
        }

        val today = LocalDate.now()
        val endDate = membership.endDate

        if (endDate.isBefore(today)) {
            return VisualAlertResponse(document, fullName, "RED", 0, "Membresía vencida el $endDate.", user.id)
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, endDate)
        return buildAlertByDays(document, fullName, daysRemaining, user.id)
    }

    private fun buildAlertByDays(
        document: String,
        fullName: String,
        daysRemaining: Long,
        userId: Int?,
    ): VisualAlertResponse =
        when {
            daysRemaining > 7 -> {
                VisualAlertResponse(
                    document,
                    fullName,
                    "GREEN",
                    daysRemaining,
                    "Acceso permitido. Vigente por $daysRemaining días.",
                    userId,
                )
            }
            else -> {
                VisualAlertResponse(
                    document,
                    fullName,
                    "YELLOW",
                    daysRemaining,
                    "¡Atención! Quedan $daysRemaining días o menos. Recordar cobrar renovación.",
                    userId,
                )
            }
        }

    private fun User.fullName() = "${this.name} ${this.lastname}"
}
