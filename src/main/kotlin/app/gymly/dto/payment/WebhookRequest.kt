package app.gymly.dto.payment

data class WebhookRequest(
    val action: String?,
    val type: String?,
    val data: NotificationData?,
)
