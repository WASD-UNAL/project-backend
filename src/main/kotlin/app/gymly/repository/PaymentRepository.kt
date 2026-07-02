package app.gymly.repository

import app.gymly.model.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Int> {
    fun findByIdOrNull(id: Int): Payment? = findById(id).orElse(null)
}
