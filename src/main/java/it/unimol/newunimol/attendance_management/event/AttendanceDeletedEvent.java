package it.unimol.newunimol.attendance_management.event;

import java.io.Serializable;
import java.time.LocalDate;

public class AttendanceDeletedEvent implements Serializable {
    private String attendanceId;
    private String studentId;
    private String courseId;
    private LocalDate lessonDate;

    public AttendanceDeletedEvent() {}

    public AttendanceDeletedEvent(String attendanceId, String studentId, String courseId, LocalDate lessonDate) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonDate = lessonDate;
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }
} 