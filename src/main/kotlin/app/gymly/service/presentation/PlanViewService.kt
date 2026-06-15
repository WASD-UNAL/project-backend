package app.gymly.service.presentation

import app.gymly.dto.membership.PlanResponse
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanViewService(
    private val planRepository: PlanRepository,
) {
    fun getActivePlans(): List<PlanResponse> = planRepository.findByActiveTrue().map { toResponseDTO(it) }

    fun getPlanById(id: Int): PlanResponse? = planRepository.findById(id).map { toResponseDTO(it) }.orElse(null)

    private fun toResponseDTO(plan: Plan): PlanResponse =
        PlanResponse(
            id = plan.id,
            name = plan.name,
            description = plan.description,
            durationDays = plan.durationDays,
            price = plan.price,
            active = plan.active,
        )
}
