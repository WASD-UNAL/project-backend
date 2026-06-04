package app.gymly.service.presentation

import app.gymly.dto.membership.PlanResponseDTO
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanViewService(private val planRepository: PlanRepository) {

    fun getActivePlans(): List<PlanResponseDTO> {
        return planRepository.findByActiveTrue().map { toResponseDTO(it) }
    }

    fun getPlanById(id: Int): PlanResponseDTO? {
        val plan = planRepository.findById(id).orElse(null) ?: return null
        return toResponseDTO(plan)
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
