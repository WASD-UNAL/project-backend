package app.gymly.controller.presentation

import app.gymly.dto.membership.PlanResponse
import app.gymly.service.presentation.PlanViewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/plans")
class PlanViewController(
    private val planViewService: PlanViewService,
) {
    @GetMapping
    fun getAllActivePlans(): ResponseEntity<List<PlanResponse>> {
        val plans = planViewService.getActivePlans()
        return ResponseEntity.ok(plans)
    }

    @GetMapping("/{id}")
    fun getPlanById(
        @PathVariable id: Int,
    ): ResponseEntity<PlanResponse> {
        val plan = planViewService.getPlanById(id)
        return if (plan != null) {
            ResponseEntity.ok(plan)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
