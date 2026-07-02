package app.gymly.repository

import app.gymly.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByDocument(document: String): User?

    fun findByEmail(email: String): User?

    @Query(
        """
        SELECT u FROM User u
        WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(u.document) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :query, '%'))
        """,
    )
    fun searchByQuery(
        @Param("query") query: String,
    ): List<User>
}
