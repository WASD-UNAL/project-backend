package app.gymly.config

import app.gymly.constants.AppConstants
import app.gymly.model.Role
import app.gymly.repository.RoleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(1)
@Component
class RoleSeeder(private val roleRepository: RoleRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        listOf(AppConstants.ROLE_CLIENT, AppConstants.ROLE_ADMIN).forEach { name ->
            if (roleRepository.findByName(name) == null) {
                roleRepository.save(Role(name = name))
            }
        }
    }
}
