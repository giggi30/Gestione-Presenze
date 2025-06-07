package it.unimol.newunimol.attendance_management.DTO;

public record AttendanceStatsDTO(
    String studentId,
    String courseId,
    double attendancePercentage
) {}
