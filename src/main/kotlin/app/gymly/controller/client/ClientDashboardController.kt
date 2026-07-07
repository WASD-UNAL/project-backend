package app.gymly.controller.client

import app.gymly.dto.client.ClientDashboardResponse
import app.gymly.service.client.ClientDashboardService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/client")
class ClientDashboardController(
    private val clientDashboardService: ClientDashboardService,
) {
    @GetMapping("/dashboard")
    fun getDashboard(authentication: Authentication): ResponseEntity<ClientDashboardResponse> {
        val userId =
            authentication.name.toIntOrNull()
                ?: throw IllegalArgumentException("Token inválido: no se pudo obtener el ID de usuario")
        return ResponseEntity.ok(clientDashboardService.getDashboard(userId))
    }
}
