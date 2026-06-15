package app.gymly.controller.auth

import app.gymly.constants.AppConstants
import app.gymly.dto.auth.CreateAdminRequest
import app.gymly.dto.auth.UserResponse
import app.gymly.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
class AdminUserController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun createAdmin(
        @Valid @RequestBody request: CreateAdminRequest,
    ): ResponseEntity<UserResponse> = ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(request))
}
