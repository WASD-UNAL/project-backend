package app.gymly.scheduler

import app.gymly.model.NotificationQueue
import app.gymly.repository.NotificationQueueRepository
import app.gymly.service.stats.VisualAlertService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val visualAlertService: VisualAlertService,
    private val notificationQueueRepository: NotificationQueueRepository,
) {
    @Scheduled(cron = "0 0 0 * * ?")
    fun processDailyAlerts() {
        val yellowAlerts = visualAlertService.getCustomersByAlertStatus("YELLOW")
        yellowAlerts.forEach { alert ->
            val userId = alert.userId ?: return@forEach
            if (!notificationQueueRepository.existsByUserIdAndProcessedFalseAndAlertType(userId, "YELLOW")) {
                notificationQueueRepository.save(
                    NotificationQueue(
                        userId = userId,
                        message = "Tu membresía vencerá en ${alert.daysRemaining} días.",
                        alertType = "YELLOW",
                    ),
                )
            }
        }
    }
}
