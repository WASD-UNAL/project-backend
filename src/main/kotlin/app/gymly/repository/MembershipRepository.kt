package app.gymly.repository

import app.gymly.model.Membership
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MembershipRepository : JpaRepository<Membership, Int> {
    fun findFirstByUserIdOrderByIdDesc(userId: Int): Membership?

    fun findByUserId(userId: Int): List<Membership>

    fun findByIdOrNull(id: Int): Membership? = findById(id).orElse(null)
}
