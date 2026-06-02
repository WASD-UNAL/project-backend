package app.gymly.controller.membership

import app.gymly.model.Plan
import app.gymly.service.membership.PlanManagementService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/plans")
class PlanManagementController(private val planManagementService: PlanManagementService) {

    @PostMapping
    fun createPlan(@RequestBody plan: Plan): ResponseEntity<Plan> {
        val createdPlan = planManagementService.createPlan(plan)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan)
    }

    @PutMapping("/{id}")
    fun updatePlan(@PathVariable id: Int, @RequestBody plan: Plan): ResponseEntity<Plan> {
        val updated = planManagementService.updatePlan(id, plan)
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deletePlan(@PathVariable id: Int): ResponseEntity<Unit> {
        val deleted = planManagementService.deletePlan(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}