package app.gymly.controller.stats

import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.service.stats.VisualAlertService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class VisualAlertController(private val visualAlertService: VisualAlertService) {

    @GetMapping("stats/visual-alert/{document}")
    fun getAccessAlert(@PathVariable document: String): ResponseEntity<VisualAlertResponse> {
        val alert = visualAlertService.checkAccessColor(document)
        return ResponseEntity.ok(alert)
    }
    @GetMapping("admin/stats/inactive-customers")
    fun getInactiveCustomers(): ResponseEntity<List<VisualAlertResponse>> {
        val inactiveCustomers = visualAlertService.getCustomersByAlertStatus("RED")
        return ResponseEntity.ok(inactiveCustomers)
    }
}