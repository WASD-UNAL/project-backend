package app.gymly.service.membership

import app.gymly.dto.membership.PlanCreateDTO
import app.gymly.dto.membership.PlanResponseDTO
import app.gymly.dto.membership.PlanUpdateDTO
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanManagementService(private val planRepository: PlanRepository) {

    fun createPlan(dto: PlanCreateDTO): PlanResponseDTO {
        val plan = toEntity(dto)
        val savedPlan = planRepository.save(plan)
        return toResponseDTO(savedPlan)
    }

    fun updatePlan(id: Int, dto: PlanUpdateDTO): PlanResponseDTO? {
        val existingPlan = planRepository.findById(id).orElse(null) ?: return null

        dto.name?.let { existingPlan.name = it }
        dto.description?.let { existingPlan.description = it }
        dto.durationDays?.let { existingPlan.durationDays = it }
        dto.price?.let { existingPlan.price = it }
        dto.active?.let { existingPlan.active = it }

        val savedPlan = planRepository.save(existingPlan)
        return toResponseDTO(savedPlan)
    }

    fun deletePlan(id: Int): Boolean {
        return if (planRepository.existsById(id)) {
            planRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    private fun toEntity(dto: PlanCreateDTO): Plan {
        return Plan(
            id = 0,
            name = dto.name,
            description = dto.description,
            durationDays = dto.durationDays,
            price = dto.price,
            active = dto.active
        )
    }

    private fun toResponseDTO(entity: Plan): PlanResponseDTO {
        return PlanResponseDTO(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            durationDays = entity.durationDays,
            price = entity.price,
            active = entity.active
        )
    }
}