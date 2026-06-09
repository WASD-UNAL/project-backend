package app.gymly.repository

import app.gymly.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Int> {
    fun findByToken(token: String): RefreshToken?
    fun deleteAllByUserId(userId: Int)
}

