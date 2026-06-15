package app.gymly.controller

import app.gymly.model.Role
import app.gymly.service.RoleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class RoleController(
    private val roleService: RoleService,
) {
    @PostMapping("/rol")
    fun createRole(
        @RequestBody newRole: Role,
    ): Role = roleService.createRole(newRole)

    @GetMapping("/roles")
    fun listRoles(): List<Role> = roleService.listRoles()
}
