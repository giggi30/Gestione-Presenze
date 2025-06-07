package it.unimol.newunimol.attendance_management.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AttendanceStatsGeneratedEvent implements Serializable {
    private String requestId;
    private String studentId; // può essere null per statistiche di corso
    private String courseId;
    private Double totalLessons;
    private Double presentLessons; // può essere null per medie corso
    private Double attendancePercentage; // può essere null per medie corso
    private Double averagePresencesPerLesson; // può essere null per statistiche studente
    private LocalDateTime timestamp;

    public AttendanceStatsGeneratedEvent() {}

    public AttendanceStatsGeneratedEvent(String requestId, String studentId, String courseId, Double totalLessons, Double presentLessons, Double attendancePercentage, Double averagePresencesPerLesson, LocalDateTime timestamp) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.totalLessons = totalLessons;
        this.presentLessons = presentLessons;
        this.attendancePercentage = attendancePercentage;
        this.averagePresencesPerLesson = averagePresencesPerLesson;
        this.timestamp = timestamp;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Double getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Double totalLessons) { this.totalLessons = totalLessons; }

    public Double getPresentLessons() { return presentLessons; }
    public void setPresentLessons(Double presentLessons) { this.presentLessons = presentLessons; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public Double getAveragePresencesPerLesson() { return averagePresencesPerLesson; }
    public void setAveragePresencesPerLesson(Double averagePresencesPerLesson) { this.averagePresencesPerLesson = averagePresencesPerLesson; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 