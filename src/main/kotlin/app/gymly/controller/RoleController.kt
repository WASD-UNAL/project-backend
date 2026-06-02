package app.gymly.controller

import app.gymly.model.Role
import app.gymly.service.RoleService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/" )
class RoleController(private val roleService: RoleService) {

    @PostMapping("/rol")
    fun createRole(@RequestBody newRole: Role): Role {
        return roleService.createRole(newRole)
    }

    @GetMapping("/roles")
    fun listRoles(): List<Role> = roleService.listRoles()
}