package app.gymly.controller.client

import app.gymly.constants.AppConstants
import app.gymly.dto.client.ClientResponse
import app.gymly.dto.client.ClientUpdateRequest
import app.gymly.service.client.ClientManagementService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/clients")
@PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
class ClientManagementController(
    private val clientManagementService: ClientManagementService,
) {
    @GetMapping
    fun searchClients(
        @RequestParam(required = false) query: String?,
    ): ResponseEntity<List<ClientResponse>> = ResponseEntity.ok(clientManagementService.searchClients(query))

    @GetMapping("/{id}")
    fun getClient(
        @PathVariable id: Int,
    ): ResponseEntity<ClientResponse> {
        val client = clientManagementService.getClient(id)
        return if (client != null) ResponseEntity.ok(client) else ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun updateClient(
        @PathVariable id: Int,
        @Valid @RequestBody request: ClientUpdateRequest,
    ): ResponseEntity<ClientResponse> {
        val updated = clientManagementService.updateClient(id, request)
        return if (updated != null) ResponseEntity.ok(updated) else ResponseEntity.notFound().build()
    }
}
