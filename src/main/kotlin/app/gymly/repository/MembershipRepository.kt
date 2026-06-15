package app.gymly.repository

import app.gymly.model.Membership
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MembershipRepository : JpaRepository<Membership, Int> {
    fun findFirstByUserIdOrderByEndDateDesc(userId: Int): Membership?
}
