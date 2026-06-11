package app.gymly.repository

import app.gymly.model.NotificationQueue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationQueueRepository : JpaRepository<NotificationQueue, Int> {
    fun findByProcessedFalse(): List<NotificationQueue>
    fun existsByUserIdAndProcessedFalseAndAlertType(userId: Int, alertType: String): Boolean
}
