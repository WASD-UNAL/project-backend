package app.gymly.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "membership_id", nullable = false)
    val membershipId: Int,
    @Column(name = "user_id", nullable = false)
    val userId: Int,
    @Column(name = "discount_id")
    val discountId: Int? = null,
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    @Column(nullable = false)
    val method: PaymentMethod = PaymentMethod.CASH,
    @Column(columnDefinition = "TEXT")
    val reference: String? = null,
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.PENDING,
    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: OffsetDateTime? = null,
)
