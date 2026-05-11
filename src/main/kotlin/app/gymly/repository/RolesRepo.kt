package app.gymly.repository

import app.gymly.model.Roles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RolesRepo : JpaRepository<Roles, Int>