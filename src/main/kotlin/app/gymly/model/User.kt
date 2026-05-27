package app.gymly.model

import jakarta.persistence.*


@Entity
@Table(name = "users")
class User(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    var role: Role,
    var name: String,
    @Column(name = "last_name")
    var lastname: String,
    var email: String,
    var password: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}