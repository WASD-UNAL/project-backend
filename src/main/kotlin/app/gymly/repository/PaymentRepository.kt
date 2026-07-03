package app.gymly.repository

import app.gymly.model.Payment
import app.gymly.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.OffsetDateTime

@Repository
interface PaymentRepository : JpaRepository<Payment, Int> {
    fun findByIdOrNull(id: Int): Payment? = findById(id).orElse(null)

    fun findByUserIdOrderByCreatedAtDesc(userId: Int): List<Payment>
    fun countByCreatedAtBetweenAndStatus(
        start: OffsetDateTime,
        end: OffsetDateTime,
        status: PaymentStatus,
    ): Long

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.status = :status")
    fun sumAmountByCreatedAtBetweenAndStatus(
        @Param("start") start: OffsetDateTime,
        @Param("end") end: OffsetDateTime,
        @Param("status") status: PaymentStatus,
    ): BigDecimal

    @Query("""
        SELECT pl.id, pl.name, COALESCE(SUM(p.amount), 0), COUNT(p.id)
        FROM Payment p, Membership m, Plan pl
        WHERE p.membershipId = m.id
          AND m.planId = pl.id
          AND p.createdAt BETWEEN :start AND :end
          AND p.status = :status
        GROUP BY pl.id, pl.name
        ORDER BY COALESCE(SUM(p.amount), 0) DESC
    """)
    fun sumAmountGroupedByPlanInPeriod(
        @Param("start") start: OffsetDateTime,
        @Param("end") end: OffsetDateTime,
        @Param("status") status: PaymentStatus,
    ): List<Array<Any>>
}
