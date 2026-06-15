package app.gymly.service.membership

import app.gymly.dto.membership.PlanRequest
import app.gymly.dto.membership.PlanResponse
import app.gymly.dto.membership.UpdatePlanRequest
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanManagementService(
    private val planRepository: PlanRepository,
) {
    fun createPlan(planRequest: PlanRequest): PlanResponse {
        val plan = toEntity(planRequest)
        val savedPlan = planRepository.save(plan)
        return toResponseDTO(savedPlan)
    }

    fun updatePlan(
        id: Int,
        updatePlanRequest: UpdatePlanRequest,
    ): PlanResponse? {
        val existingPlan = planRepository.findById(id).orElse(null) ?: return null

        updatePlanRequest.name?.let { existingPlan.name = it }
        updatePlanRequest.description?.let { existingPlan.description = it }
        updatePlanRequest.durationDays?.let { existingPlan.durationDays = it }
        updatePlanRequest.price?.let { existingPlan.price = it }
        updatePlanRequest.active?.let { existingPlan.active = it }

        val savedPlan = planRepository.save(existingPlan)
        return toResponseDTO(savedPlan)
    }

    fun deletePlan(id: Int): Boolean {
        if (!planRepository.existsById(id)) return false
        planRepository.deleteById(id)
        return true
    }

    private fun toEntity(planRequest: PlanRequest): Plan =
        Plan(
            id = null,
            name = planRequest.name,
            description = planRequest.description,
            durationDays = planRequest.durationDays,
            price = planRequest.price,
            active = planRequest.active,
        )

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
