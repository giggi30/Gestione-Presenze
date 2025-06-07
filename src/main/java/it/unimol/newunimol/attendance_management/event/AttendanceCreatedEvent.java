package it.unimol.newunimol.attendance_management.event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceCreatedEvent implements Serializable {
    private String attendanceId;
    private String studentId;
    private String courseId;
    private LocalDate lessonDate;
    private String status;
    private LocalTime orarioIngresso;
    private LocalTime orarioUscita;

    public AttendanceCreatedEvent() {}

    public AttendanceCreatedEvent(String attendanceId, String studentId, String courseId, LocalDate lessonDate, String status, LocalTime orarioIngresso, LocalTime orarioUscita) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonDate = lessonDate;
        this.status = status;
        this.orarioIngresso = orarioIngresso;
        this.orarioUscita = orarioUscita;
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalTime getOrarioIngresso() { return orarioIngresso; }
    public void setOrarioIngresso(LocalTime orarioIngresso) { this.orarioIngresso = orarioIngresso; }

    public LocalTime getOrarioUscita() { return orarioUscita; }
    public void setOrarioUscita(LocalTime orarioUscita) { this.orarioUscita = orarioUscita; }
} 