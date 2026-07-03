package app.gymly.service.presentation

import app.gymly.dto.auth.UserResponse
import app.gymly.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserProfileService(
    private val userRepository: UserRepository,
) {
    fun getMyProfile(
        userId: Int,
        roleName: String,
    ): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        return UserResponse(
            id = user.id ?: error("User id is null"),
            name = user.name,
            lastname = user.lastname,
            email = user.email,
            document = user.document,
            role = roleName,
            active = user.active,
        )
    }
}
