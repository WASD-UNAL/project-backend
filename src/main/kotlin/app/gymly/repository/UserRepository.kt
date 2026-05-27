package app.gymly.repository

import app.gymly.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByDocument(document: Int): User?
}