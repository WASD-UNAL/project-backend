package app.gymly.repository

import app.gymly.model.Discount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DiscountRepository : JpaRepository<Discount, Int> {
    @Query("SELECT DISTINCT d FROM Discount d LEFT JOIN FETCH d.plans")
    fun findAllWithPlans(): List<Discount>

    @Query(
        "SELECT DISTINCT d FROM Discount d JOIN FETCH d.plans " +
            "WHERE d.active = true AND d.initDate <= :date AND d.endDate >= :date",
    )
    fun findValidWithPlans(date: LocalDate): List<Discount>
}
