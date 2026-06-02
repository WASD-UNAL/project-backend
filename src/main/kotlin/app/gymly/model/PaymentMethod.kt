package app.gymly.model

enum class PaymentMethod(val dbValue: String) {
    CASH("cash"),
    CARD("card"),
    TRANSFER("transfer");

    companion object {
        fun fromDb(value: String): PaymentMethod? = values().firstOrNull { it.dbValue == value }
    }
}

