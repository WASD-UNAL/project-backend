package app.gymly.service.presentation

import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.model.MembershipStatus
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.stats.VisualAlertService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MembershipViewService(
    private val visualAlertService: VisualAlertService,
    private val membershipRepository: MembershipRepository,
    private val planRepository: PlanRepository,
) {
    fun getMyMembership(userId: Int): MyMembershipResponse {
        val alert = visualAlertService.checkAccessColorByUserId(userId)
        val membership = membershipRepository.findFirstByUserIdOrderByEndDateDesc(userId)

        val isActive =
            membership != null &&
                membership.status == MembershipStatus.ACTIVE &&
                !membership.endDate.isBefore(LocalDate.now())

        val plan = if (isActive) membership?.let { planRepository.findByIdOrNull(it.planId) } else null

        return MyMembershipResponse(
            hasActiveMembership = isActive,
            planId = plan?.id,
            planName = plan?.name,
            price = plan?.price,
            initDate = if (isActive) membership?.initDate else null,
            endDate = if (isActive) membership?.endDate else null,
            statusColor = alert.statusColor,
            daysRemaining = alert.daysRemaining,
            message = alert.message,
        )
    }
}
