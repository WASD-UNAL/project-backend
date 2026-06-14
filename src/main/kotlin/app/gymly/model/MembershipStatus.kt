package app.gymly.model

enum class MembershipStatus(
    val dbValue: String,
) {
    ACTIVE("active"),
    EXPIRED("expired"),
    FROZEN("frozen"),
    ;

    companion object {
        fun fromDb(value: String): MembershipStatus? = values().firstOrNull { it.dbValue == value }
    }
}
