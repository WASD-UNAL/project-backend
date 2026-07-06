package app.gymly.service.stats

import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.model.Attendance
import app.gymly.repository.AttendanceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccessControlService(
    private val visualAlertService: VisualAlertService,
    private val attendanceRepository: AttendanceRepository,
) {
    @Transactional
    fun registerAccess(document: String): VisualAlertResponse {
        val alert = visualAlertService.checkAccessColor(document)

        if (alert.userId != null && alert.statusColor != DENIED_STATUS) {
            attendanceRepository.save(Attendance(userId = alert.userId))
        }

        return alert
    }

    private companion object {
        const val DENIED_STATUS = "RED"
    }
}
