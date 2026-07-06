package app.gymly.scheduler

import app.gymly.model.NotificationQueue
import app.gymly.repository.AttendanceRepository
import app.gymly.repository.NotificationQueueRepository
import app.gymly.service.stats.VisualAlertService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationSchedulerTest {
    private lateinit var visualAlertService: VisualAlertService
    private lateinit var notificationQueueRepository: NotificationQueueRepository
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var scheduler: NotificationScheduler

    @BeforeEach
    fun setUp() {
        visualAlertService = mock()
        notificationQueueRepository = mock()
        attendanceRepository = mock()
        scheduler = NotificationScheduler(visualAlertService, notificationQueueRepository, attendanceRepository)
    }

    @Test
    fun enqueuesInactivityNotificationForInactiveUsers() {
        whenever(attendanceRepository.findUserIdsInactiveSince(any())).thenReturn(listOf(5))
        whenever(notificationQueueRepository.existsByUserIdAndProcessedFalseAndAlertType(5, "INACTIVITY"))
            .thenReturn(false)

        scheduler.processInactivityAlerts()

        val captor = argumentCaptor<NotificationQueue>()
        verify(notificationQueueRepository).save(captor.capture())
        Assertions.assertEquals(5, captor.firstValue.userId)
        Assertions.assertEquals("INACTIVITY", captor.firstValue.alertType)
    }

    @Test
    fun doesNotEnqueueWhenAnInactivityNotificationIsAlreadyPending() {
        whenever(attendanceRepository.findUserIdsInactiveSince(any())).thenReturn(listOf(5))
        whenever(notificationQueueRepository.existsByUserIdAndProcessedFalseAndAlertType(5, "INACTIVITY"))
            .thenReturn(true)

        scheduler.processInactivityAlerts()

        verify(notificationQueueRepository, never()).save(any())
    }

    @Test
    fun doesNothingWhenNoUsersAreInactive() {
        whenever(attendanceRepository.findUserIdsInactiveSince(any())).thenReturn(emptyList())

        scheduler.processInactivityAlerts()

        verify(notificationQueueRepository, never())
            .existsByUserIdAndProcessedFalseAndAlertType(any(), eq("INACTIVITY"))
        verify(notificationQueueRepository, never()).save(any())
    }
}
