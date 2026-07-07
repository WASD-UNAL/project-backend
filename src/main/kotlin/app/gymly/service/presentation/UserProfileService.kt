package app.gymly.service.presentation

import app.gymly.dto.auth.ChangePasswordRequest
import app.gymly.dto.auth.UpdateProfileRequest
import app.gymly.dto.auth.UserResponse
import app.gymly.exception.InvalidCredentialsException
import app.gymly.model.User
import app.gymly.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserProfileService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun getMyProfile(
        userId: Int,
        roleName: String,
    ): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        return user.toResponse(roleName)
    }

    @Transactional
    fun updateMyProfile(
        userId: Int,
        roleName: String,
        request: UpdateProfileRequest,
    ): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        request.name?.let { user.name = it.trim() }
        request.lastname?.let { user.lastname = it.trim() }
        request.email?.let { user.email = it.trim() }
        request.phone?.let { user.phone = it.trim().ifEmpty { null } }
        request.weight?.let { user.weight = it }
        request.height?.let { user.height = it }
        return userRepository.save(user).toResponse(roleName)
    }

    @Transactional
    fun changeMyPassword(
        userId: Int,
        request: ChangePasswordRequest,
    ) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw InvalidCredentialsException("La contraseña actual no es correcta.")
        }
        user.passwordHash = passwordEncoder.encode(request.newPassword) ?: throw RuntimeException("Password encoding failed")
        userRepository.save(user)
    }

    private fun User.toResponse(roleName: String) =
        UserResponse(
            id = id ?: error("User id is null"),
            name = name,
            lastname = lastname,
            email = email,
            document = document,
            role = roleName,
            active = active,
            phone = phone,
            weight = weight,
            height = height,
        )
}
