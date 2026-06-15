package app.gymly.repository

import app.gymly.model.Discount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DiscountRepository : JpaRepository<Discount, Int>
