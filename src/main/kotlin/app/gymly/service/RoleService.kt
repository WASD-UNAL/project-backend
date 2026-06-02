package app.gymly.service

import app.gymly.model.Role
import app.gymly.repository.RoleRepository
import org.springframework.stereotype.Service

@Service
class RoleService(private val roleRepository: RoleRepository) {
    fun createRole(newRole: Role): Role {
        return roleRepository.save(newRole)
    }
    fun listRoles(): List<Role> = roleRepository.findAll()
}