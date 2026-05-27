package app.gymly.controller.presentation
import app.gymly.model.Plan
import app.gymly.service.presentation.PlanViewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/plans")
class PlanViewController(private val planViewService: PlanViewService) {

    @GetMapping
    fun getAllActivePlans(): ResponseEntity<List<Plan>> {
        val plans = planViewService.getActivePlans()
        return ResponseEntity.ok(plans)
    }

    @GetMapping("/{id}")
    fun getPlanById(@PathVariable id: Int): ResponseEntity<Plan> {
        val plan = planViewService.getPlanById(id)
        return if (plan != null) {
            ResponseEntity.ok(plan)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}