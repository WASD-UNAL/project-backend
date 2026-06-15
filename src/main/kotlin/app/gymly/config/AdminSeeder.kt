package app.gymly.config

import app.gymly.constants.AppConstants
import app.gymly.model.Role
import app.gymly.model.User
import app.gymly.repository.RoleRepository
import app.gymly.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Order(2)
@Component
class AdminSeeder(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value($$"${admin.password}") private val password: String,
) : CommandLineRunner {
    override fun run(vararg args: String) {
        val adminRole =
            roleRepository.findByName(AppConstants.ROLE_ADMIN)
                ?: roleRepository.save(Role(name = AppConstants.ROLE_ADMIN))

        if (userRepository.count() == 0L) {
            val firstAdmin =
                User(
                    name = "Admin",
                    lastname = "Supremo",
                    email = "admin@sistema.com",
                    document = "1002220000",
                    passwordHash = passwordEncoder.encode(password)!!,
                    roleId = adminRole.id ?: error("Role ID no generado"),
                    active = true,
                )
            userRepository.save(firstAdmin)
        }
    }
}
