package app.gymly.repository

import app.gymly.model.Payment
import app.gymly.model.Plan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanRepository : JpaRepository<Plan, Int> {
    fun findByActiveTrue(): List<Plan>
    fun findByIdOrNull(id: Int): Plan? = findById(id).orElse(null)
}
