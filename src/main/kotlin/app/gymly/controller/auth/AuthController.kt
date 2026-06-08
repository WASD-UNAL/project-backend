package app.gymly.controller.auth

import app.gymly.dto.auth.AuthResponse
import app.gymly.dto.auth.LoginRequest
import app.gymly.dto.auth.LogoutResponse
import app.gymly.dto.auth.RefreshTokenRequest
import app.gymly.dto.auth.RegisterRequest
import app.gymly.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(request))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(request))
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refreshAccessToken(request))
    }

    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<LogoutResponse> {
        val userId = authentication.name.toIntOrNull() ?: throw IllegalArgumentException("Invalid user id")
        return ResponseEntity.ok(authService.logout(userId))
    }
}
