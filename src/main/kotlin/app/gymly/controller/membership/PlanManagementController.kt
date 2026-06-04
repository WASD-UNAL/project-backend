package app.gymly.controller.membership

import app.gymly.dto.membership.PlanCreateDTO
import app.gymly.dto.membership.PlanResponseDTO
import app.gymly.dto.membership.PlanUpdateDTO
import app.gymly.service.membership.PlanManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/plans")
class PlanManagementController(private val planManagementService: PlanManagementService) {

    @PostMapping
    fun createPlan(@Valid @RequestBody dto: PlanCreateDTO): ResponseEntity<PlanResponseDTO> {
        val createdPlan = planManagementService.createPlan(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan)
    }

    @PutMapping("/{id}")
    fun updatePlan(
        @PathVariable id: Int,
        @Valid @RequestBody dto: PlanUpdateDTO
    ): ResponseEntity<PlanResponseDTO> {
        val updated = planManagementService.updatePlan(id, dto)
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