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
    var membershipId: Int,
    @Column(name = "user_id", nullable = false)
    var userId: Int,
    @Column(name = "discount_id")
    var discountId: Int? = null,
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,
    @Column(nullable = false)
    var method: PaymentMethod = PaymentMethod.CASH,
    @Column(columnDefinition = "TEXT")
    var reference: String? = null,
    // external_reference enviado a Mercado Pago; debe ser único globalmente porque
    // la cuenta de MP acumula pagos de otros entornos y reinicios de base de datos.
    @Column(name = "checkout_reference", unique = true)
    var checkoutReference: String? = null,
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
)
