package app.gymly.service.membership

import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanManagementService(private val planRepository: PlanRepository) {

    fun createPlan(plan: Plan): Plan {
        return planRepository.save(plan)
    }

    fun updatePlan(id: Int, updatedPlan: Plan): Plan? {
        if (!planRepository.existsById(id)) return null

        val planToSave = Plan(
            id = id,
            name = updatedPlan.name,
            description = updatedPlan.description,
            durationDays = updatedPlan.durationDays,
            price = updatedPlan.price,
            active = updatedPlan.active
        )
        return planRepository.save(planToSave)
    }

    fun deletePlan(id: Int): Boolean {
        return if (planRepository.existsById(id)) {
            planRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}