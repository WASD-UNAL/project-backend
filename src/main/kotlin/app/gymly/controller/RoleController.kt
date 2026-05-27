package app.gymly.controller

import app.gymly.model.Role
import app.gymly.repository.RoleRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/", )
class RoleController(private val roleRepository: RoleRepository) {

    @PostMapping("/rol")
    fun createRole(@RequestBody newRole: Role): Role {
        return roleRepository.save(newRole)
    }

    @GetMapping("/roles")
    fun listRoles(): List<Role> = roleRepository.findAll()
}