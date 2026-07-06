package app.gymly.controller.stats

import app.gymly.dto.stats.AttendanceCheckInRequest
import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.service.stats.AccessControlService
import app.gymly.service.stats.VisualAlertService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class VisualAlertController(
    private val visualAlertService: VisualAlertService,
    private val accessControlService: AccessControlService,
) {
    @GetMapping("stats/visual-alert/{document}")
    fun getAccessAlert(
        @PathVariable document: String,
    ): ResponseEntity<VisualAlertResponse> {
        val alert = visualAlertService.checkAccessColor(document)
        return ResponseEntity.ok(alert)
    }

    @PostMapping("admin/access")
    fun registerAccess(
        @Valid @RequestBody request: AttendanceCheckInRequest,
    ): ResponseEntity<VisualAlertResponse> {
        val alert = accessControlService.registerAccess(request.document)
        return ResponseEntity.ok(alert)
    }

    @GetMapping("admin/stats/inactive-customers")
    fun getInactiveCustomers(): ResponseEntity<List<VisualAlertResponse>> {
        val inactiveCustomers = visualAlertService.getCustomersByAlertStatus("RED")
        return ResponseEntity.ok(inactiveCustomers)
    }
}
