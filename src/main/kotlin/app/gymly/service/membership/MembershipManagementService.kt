package app.gymly.service.membership
import app.gymly.dto.membership.MembershipResponse
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PlanRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class MembershipManagementService(
    private val membershipRepository: MembershipRepository,
    private val planRepository: PlanRepository,
) {
    @Transactional(readOnly = true)
    fun getMembershipById(id: Int): MembershipResponse {
        val membership =
            membershipRepository.findByIdOrNull(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía con ID $id no encontrada")
        return toResponse(membership)
    }

    @Transactional(readOnly = true)
    fun getMembershipsByUserId(userId: Int): List<MembershipResponse> = membershipRepository.findByUserId(userId).map { toResponse(it) }

    @Transactional
    fun activateMembership(membershipId: Int) {
        val membership =
            membershipRepository.findByIdOrNull(membershipId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía con ID $membershipId no encontrada")

        val plan =
            planRepository.findByIdOrNull(membership.planId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan con ID ${membership.planId} no encontrado")

        val now = LocalDate.now()
        val durationDays = plan.durationDays.toLong()

        membership.status = MembershipStatus.ACTIVE
        membership.initDate = now
        membership.endDate = now.plusDays(durationDays)

        membershipRepository.save(membership)
    }

    private fun toResponse(membership: Membership): MembershipResponse =
        MembershipResponse(
            id = membership.id ?: 0,
            userId = membership.userId,
            planId = membership.planId,
            initDate = membership.initDate,
            endDate = membership.endDate,
            status = membership.status,
        )
}
