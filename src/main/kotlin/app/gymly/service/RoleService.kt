package app.gymly.service

import app.gymly.model.Role
import app.gymly.repository.RoleRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody

@Service
class RoleService(private val roleRepository: RoleRepository) {
    fun createRole(@RequestBody newRole: Role): Role {
        return roleRepository.save(newRole)
    }
    fun listRoles(): List<Role> = roleRepository.findAll()
}