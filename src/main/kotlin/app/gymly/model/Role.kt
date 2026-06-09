package app.gymly.model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    varid: Int? = null,
    @Column(nullable = false)
    var name: String
)