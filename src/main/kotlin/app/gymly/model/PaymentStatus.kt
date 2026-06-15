package app.gymly.model

enum class PaymentStatus(
    val dbValue: String,
) {
    SUCCESSFUL("successful"),
    PENDING("pending"),
    REJECTED("rejected"),
    ;

    companion object {
        fun fromDb(value: String): PaymentStatus? = values().firstOrNull { it.dbValue == value }
    }
}
