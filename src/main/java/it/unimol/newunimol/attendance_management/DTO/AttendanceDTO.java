package it.unimol.newunimol.attendance_management.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDTO(
    String attendanceId,
    String studentId,
    String courseId,
    LocalDate lessonDate,
    String status,
    LocalTime orarioIngresso,
    LocalTime orarioUscita
) {
}
