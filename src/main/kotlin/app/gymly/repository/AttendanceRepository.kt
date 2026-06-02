package app.gymly.repository

import app.gymly.model.Attendance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Int>

