package it.unimol.newunimol.attendance_management.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceCreateDTO(
    String studentId,
    String courseId,
    LocalDate lessonDate,
    String status,
    LocalTime orarioIngresso,
    LocalTime orarioUscita
) {
}
