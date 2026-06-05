package app.gymly.repository

import app.gymly.model.Attendance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Int> {
    fun findByDateBetween(start: OffsetDateTime, end: OffsetDateTime): List<Attendance>
}

