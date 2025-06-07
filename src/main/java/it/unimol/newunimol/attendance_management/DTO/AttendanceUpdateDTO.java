package it.unimol.newunimol.attendance_management.DTO;

import java.time.LocalTime;

public record AttendanceUpdateDTO(
    String status,
    LocalTime orarioIngresso,
    LocalTime orarioUscita
) {
}