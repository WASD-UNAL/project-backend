package app.gymly.controller.stats

import app.gymly.dto.ColorAlert
import app.gymly.service.stats.ColorAlertService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stats/color-alert")
class ColorAlert(private val colorAlertService: ColorAlertService) {

    @GetMapping("/{document}")
    fun getAccessAlert(@PathVariable document: Int): ResponseEntity<ColorAlert> {
        val alert = colorAlertService.checkAccessColor(document)
        return ResponseEntity.ok(alert)
    }
}