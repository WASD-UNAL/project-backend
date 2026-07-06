package app.gymly.scheduler

import app.gymly.model.NotificationQueue
import app.gymly.repository.AttendanceRepository
import app.gymly.repository.NotificationQueueRepository
import app.gymly.service.stats.VisualAlertService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class NotificationScheduler(
    private val visualAlertService: VisualAlertService,
    private val notificationQueueRepository: NotificationQueueRepository,
    private val attendanceRepository: AttendanceRepository,
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

    @Scheduled(cron = "0 0 1 * * ?")
    fun processInactivityAlerts() {
        val cutoff = OffsetDateTime.now().minusDays(INACTIVITY_THRESHOLD_DAYS)
        attendanceRepository.findUserIdsInactiveSince(cutoff).forEach { userId ->
            if (!notificationQueueRepository.existsByUserIdAndProcessedFalseAndAlertType(userId, INACTIVITY_ALERT_TYPE)) {
                notificationQueueRepository.save(
                    NotificationQueue(
                        userId = userId,
                        message = "¡Te extrañamos! Llevas varios días sin asistir al gimnasio. Vuelve y retoma tu constancia.",
                        alertType = INACTIVITY_ALERT_TYPE,
                    ),
                )
            }
        }
    }

    private companion object {
        const val INACTIVITY_THRESHOLD_DAYS = 3L
        const val INACTIVITY_ALERT_TYPE = "INACTIVITY"
    }
}
