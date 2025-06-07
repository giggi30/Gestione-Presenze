package it.unimol.newunimol.attendance_management.DTO;

public record CourseAttendanceStatsDTO(
    String courseId,
    double averageAttendance
) {}
