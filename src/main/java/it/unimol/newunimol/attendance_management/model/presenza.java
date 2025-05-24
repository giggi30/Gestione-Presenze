package it.unimol.newunimol.attendance_management.model;

import java.time.LocalDate;

public record presenza(
    String attendanceId,
    String studentId,
    String courseId,
    LocalDate lessonDate,
    String status
) {
    public presenza(String attendanceId, String studentId, String courseId, LocalDate lessonDate, String status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonDate = lessonDate;
        this.status = status;
    }
}