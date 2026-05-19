package app.gymly.controller.presentation
import app.gymly.model.Plan
import app.gymly.service.presentation.PlanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/plans")
class PlanController(private val planService: PlanService) {

    @GetMapping
    fun getAllActivePlans(): ResponseEntity<List<Plan>> {
        val plans = planService.getActivePlans()
        return ResponseEntity.ok(plans)
    }

    @GetMapping("/{id}")
    fun getPlanById(@PathVariable id: Int): ResponseEntity<Plan> {
        val plan = planService.getPlanById(id)
        return if (plan != null) {
            ResponseEntity.ok(plan)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}