package app.gymly.controller

import app.gymly.model.Role
import app.gymly.repository.RoleRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/", )
class RoleController(private val roleRepository: RoleRepository) {

    @GetMapping
    fun status() = mapOf("message" to "Backend levantado con éxito")

    @PostMapping("/api/rol")
    fun createRole(@RequestBody newRole: Role): Role {
        return roleRepository.save(newRole)
    }

    @GetMapping("/api/roles")
    fun listRoles(): List<Role> = roleRepository.findAll()
}