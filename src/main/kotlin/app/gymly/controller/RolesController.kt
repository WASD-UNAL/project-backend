package app.gymly.controller

import app.gymly.model.Roles
import app.gymly.repository.RolesRepo
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class RolesController(private val repo: RolesRepo) {

    @GetMapping
    fun status() = mapOf("mensaje" to "Backend levantado con éxito")

    @PostMapping("/rol")
    fun crearRol(@RequestBody nuevoRol: Roles): Roles {
        return repo.save(nuevoRol)
    }

    @GetMapping("/roles")
    fun listarRoles(): List<Roles> = repo.findAll()
}