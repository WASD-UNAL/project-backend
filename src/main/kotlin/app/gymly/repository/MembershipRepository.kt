package app.gymly.repository

import app.gymly.model.Membership
import app.gymly.model.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MembershipRepository : JpaRepository<Membership, Int> {
    fun findFirstByUserIdOrderByEndDateDesc(userId: Int): Membership?
    fun findByIdOrNull(id: Int): Membership? = findById(id).orElse(null)
}
