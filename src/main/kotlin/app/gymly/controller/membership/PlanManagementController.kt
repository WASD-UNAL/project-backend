package app.gymly.controller.membership

import app.gymly.dto.membership.PlanRequest
import app.gymly.dto.membership.PlanResponse
import app.gymly.dto.membership.UpdatePlanRequest
import app.gymly.service.membership.PlanManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/plans")
class PlanManagementController(private val planManagementService: PlanManagementService) {

    @PostMapping
    fun createPlan(@Valid @RequestBody planRequest: PlanRequest): ResponseEntity<PlanResponse> {
        val createdPlan = planManagementService.createPlan(planRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan)
    }

    @PutMapping("/{id}")
    fun updatePlan(
        @PathVariable id: Int,
        @Valid @RequestBody updatePlanRequest: UpdatePlanRequest
    ): ResponseEntity<PlanResponse> {
        val updated = planManagementService.updatePlan(id, updatePlanRequest)
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