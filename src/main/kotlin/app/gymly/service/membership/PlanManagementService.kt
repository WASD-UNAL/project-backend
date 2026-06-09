package app.gymly.service.membership

import app.gymly.dto.membership.PlanCreate
import app.gymly.dto.membership.PlanResponse
import app.gymly.dto.membership.PlanUpdate
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanManagementService(private val planRepository: PlanRepository) {

    fun createPlan(planCreate: PlanCreate): PlanResponse {
        val plan = toEntity(planCreate)
        val savedPlan = planRepository.save(plan)
        return toResponseDTO(savedPlan)
    }

    fun updatePlan(id: Int, planUpdate: PlanUpdate): PlanResponse? {
        val existingPlan = planRepository.findById(id).orElse(null) ?: return null

        planUpdate.name?.let { existingPlan.name = it }
        planUpdate.description?.let { existingPlan.description = it }
        planUpdate.durationDays?.let { existingPlan.durationDays = it }
        planUpdate.price?.let { existingPlan.price = it }
        planUpdate.active?.let { existingPlan.active = it }

        val savedPlan = planRepository.save(existingPlan)
        return toResponseDTO(savedPlan)
    }

    fun deletePlan(id: Int): Boolean {
        if (!planRepository.existsById(id)) return false
        planRepository.deleteById(id)
        return true
    }

    private fun toEntity(planCreate: PlanCreate): Plan {
        return Plan(
            id = null,
            name = planCreate.name,
            description = planCreate.description,
            durationDays = planCreate.durationDays,
            price = planCreate.price,
            active = planCreate.active
        )
    }

    private fun toResponseDTO(plan: Plan): PlanResponse {
        return PlanResponse(
            id = plan.id,
            name = plan.name,
            description = plan.description,
            durationDays = plan.durationDays,
            price = plan.price,
            active = plan.active
        )
    }
}