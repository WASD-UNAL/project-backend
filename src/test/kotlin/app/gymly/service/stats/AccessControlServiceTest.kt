package app.gymly.service.stats

import app.gymly.dto.stats.VisualAlertResponse
import app.gymly.model.Attendance
import app.gymly.repository.AttendanceRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AccessControlServiceTest {
    private lateinit var visualAlertService: VisualAlertService
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var service: AccessControlService

    @BeforeEach
    fun setUp() {
        visualAlertService = mock()
        attendanceRepository = mock()
        service = AccessControlService(visualAlertService, attendanceRepository)
    }

    @Test
    fun registersAttendanceWhenAccessIsGranted() {
        val document = "1001"
        whenever(visualAlertService.checkAccessColor(document))
            .thenReturn(VisualAlertResponse(document, "Juan Perez", "GREEN", 20, "Acceso permitido.", 7))

        val result = service.registerAccess(document)

        val captor = argumentCaptor<Attendance>()
        verify(attendanceRepository).save(captor.capture())
        Assertions.assertEquals(7, captor.firstValue.userId)
        Assertions.assertEquals("GREEN", result.statusColor)
    }

    @Test
    fun registersAttendanceWhenMembershipIsAboutToExpire() {
        val document = "1002"
        whenever(visualAlertService.checkAccessColor(document))
            .thenReturn(VisualAlertResponse(document, "Ana Ruiz", "YELLOW", 3, "Quedan 3 días.", 9))

        service.registerAccess(document)

        verify(attendanceRepository).save(any())
    }

    @Test
    fun doesNotRegisterAttendanceWhenAccessIsDenied() {
        val document = "1003"
        whenever(visualAlertService.checkAccessColor(document))
            .thenReturn(VisualAlertResponse(document, "Luis Diaz", "RED", 0, "Membresía vencida.", 11))

        val result = service.registerAccess(document)

        verify(attendanceRepository, never()).save(any())
        Assertions.assertEquals("RED", result.statusColor)
    }

    @Test
    fun doesNotRegisterAttendanceWhenUserDoesNotExist() {
        val document = "9999"
        whenever(visualAlertService.checkAccessColor(document))
            .thenReturn(VisualAlertResponse(document, null, "RED", 0, "Usuario no registrado.", null))

        service.registerAccess(document)

        verify(attendanceRepository, never()).save(any())
    }
}
