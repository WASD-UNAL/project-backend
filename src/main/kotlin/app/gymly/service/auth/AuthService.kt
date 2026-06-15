package app.gymly.service.auth

import app.gymly.constants.AppConstants
import app.gymly.dto.auth.AuthResponse
import app.gymly.dto.auth.CreateAdminRequest
import app.gymly.dto.auth.LoginRequest
import app.gymly.dto.auth.LogoutResponse
import app.gymly.dto.auth.RefreshTokenRequest
import app.gymly.dto.auth.RegisterRequest
import app.gymly.dto.auth.UserResponse
import app.gymly.exception.InvalidCredentialsException
import app.gymly.exception.RoleNotConfiguredException
import app.gymly.model.RefreshToken
import app.gymly.model.Role
import app.gymly.model.User
import app.gymly.repository.RefreshTokenRepository
import app.gymly.repository.RoleRepository
import app.gymly.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = findByIdentifier(request.identifier) ?: throw InvalidCredentialsException()
        if (!user.active) throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) throw InvalidCredentialsException()
        val role =
            roleRepository
                .findById(user.roleId)
                .orElseThrow { RoleNotConfiguredException("roleId=${user.roleId}") }
        return buildAuthResponse(user, role)
    }

    @Transactional
    fun registerClient(request: RegisterRequest): AuthResponse {
        val role = roleRepository.findByName(AppConstants.ROLE_CLIENT) ?: throw RoleNotConfiguredException(AppConstants.ROLE_CLIENT)
        val saved = createUser(role, request.name, request.lastname, request.email, request.document, request.password)
        return buildAuthResponse(saved, role)
    }

    @Transactional
    fun createAdmin(request: CreateAdminRequest): UserResponse {
        val role = roleRepository.findByName(AppConstants.ROLE_ADMIN) ?: throw RoleNotConfiguredException(AppConstants.ROLE_ADMIN)
        val saved = createUser(role, request.name, request.lastname, request.email, request.document, request.password)
        return saved.toResponse(role.name)
    }

    @Transactional
    fun refreshAccessToken(request: RefreshTokenRequest): AuthResponse {
        val refreshToken =
            refreshTokenRepository.findByToken(request.refreshToken)
                ?: throw InvalidCredentialsException("Invalid refresh token")

        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            throw InvalidCredentialsException("Refresh token has expired")
        }

        val user =
            userRepository
                .findById(refreshToken.userId)
                .orElseThrow { InvalidCredentialsException("User not found") }

        val role =
            roleRepository
                .findById(user.roleId)
                .orElseThrow { RoleNotConfiguredException("roleId=${user.roleId}") }

        return buildAuthResponse(user, role)
    }

    @Transactional
    fun logout(userId: Int): LogoutResponse {
        refreshTokenRepository.deleteAllByUserId(userId)
        return LogoutResponse("Logged out successfully")
    }

    private fun findByIdentifier(identifier: String): User? {
        val trimmed = identifier.trim()
        if (trimmed.isEmpty()) return null
        if ('@' in trimmed) return userRepository.findByEmail(trimmed)
        return userRepository.findByDocument(trimmed) ?: userRepository.findByEmail(trimmed)
    }

    private fun createUser(
        role: Role,
        name: String,
        lastName: String,
        email: String,
        document: String,
        rawPassword: String,
    ): User {
        val user =
            User(
                roleId = role.id ?: throw RoleNotConfiguredException(role.name),
                name = name,
                lastname = lastName,
                email = email,
                passwordHash = passwordEncoder.encode(rawPassword) ?: throw RuntimeException("Password encoding failed"),
                document = document.trim(),
            )
        return userRepository.save(user)
    }

    private fun buildAuthResponse(
        user: User,
        role: Role,
    ): AuthResponse {
        val issued = jwtService.issue(user, role.name)

        // Guardar refresh token en BD
        val refreshTokenEntity =
            RefreshToken(
                userId = user.id ?: throw RuntimeException("User id is null"),
                token = issued.refreshToken,
                expiresAt = issued.refreshExpiresAt,
                createdAt = Instant.now(),
            )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            token = issued.token,
            expiresAt = issued.expiresAt,
            refreshToken = issued.refreshToken,
            refreshExpiresAt = issued.refreshExpiresAt,
            user = user.toResponse(role.name),
        )
    }

    private fun User.toResponse(roleName: String): UserResponse =
        UserResponse(
            id = id ?: error("User id is null"),
            name = name,
            lastname = lastname,
            email = email,
            document = document,
            role = roleName,
            active = active,
        )
}
