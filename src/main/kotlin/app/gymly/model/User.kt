package app.gymly.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "role_id", nullable = false)
    var roleId: Int,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var lastname: String,

    @Column(unique = true ,nullable = false)
    var document: Int,

    @Column(unique = true)
    var phone: String?,

    @Column(unique = true)
    var mail: String?,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(nullable = false)
    var active: Boolean = true
)