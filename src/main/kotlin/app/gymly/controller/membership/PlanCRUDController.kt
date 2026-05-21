package app.gymly.controller.membership

import app.gymly.model.Plan
import app.gymly.service.membership.PlanCRUDService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/membership/plans")
class MembershipPlanController(private val planCRUDService: PlanCRUDService) {

    @PostMapping
    fun createPlan(@RequestBody plan: Plan): ResponseEntity<Plan> {
        val createdPlan = planCRUDService.createPlan(plan)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan)
    }

    @PutMapping("/{id}")
    fun updatePlan(@PathVariable id: Int, @RequestBody plan: Plan): ResponseEntity<Plan> {
        val updated = planCRUDService.updatePlan(id, plan)
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deletePlan(@PathVariable id: Int): ResponseEntity<Unit> {
        val deleted = planCRUDService.deletePlan(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}