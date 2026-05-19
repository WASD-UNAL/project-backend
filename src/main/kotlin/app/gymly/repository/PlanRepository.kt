package app.gymly.repository

import app.gymly.model.Plan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlanRepository : JpaRepository<Plan, Int> {
    fun findByActiveTrue(): List<Plan>
}