package app.gymly.controller.stats

import app.gymly.dto.VisualAlertDTO
import app.gymly.service.stats.VisualAlertService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stats/color-alert")
class VisualAlertController(private val visualAlertService: VisualAlertService) {

    @GetMapping("/{document}")
    fun getAccessAlert(@PathVariable document: Int): ResponseEntity<VisualAlertDTO> {
        val alert = visualAlertService.checkAccessColor(document)
        return ResponseEntity.ok(alert)
    }
}