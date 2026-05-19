package app.gymly.service.presentation

import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service

@Service
class PlanService(private val planRepository: PlanRepository) {

    fun getActivePlans(): List<Plan> {
        return planRepository.findByActiveTrue()
    }

    fun getPlanById(id: Int): Plan? {
        return planRepository.findById(id).orElse(null)
    }
}
