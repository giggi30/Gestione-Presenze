package it.unimol.newunimol.attendance_management.event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceUpdatedEvent implements Serializable {
    private String attendanceId;
    private String oldStatus;
    private String newStatus;
    private LocalDate lessonDate;
    private LocalTime orarioIngresso;
    private LocalTime orarioUscita;

    public AttendanceUpdatedEvent() {}

    public AttendanceUpdatedEvent(String attendanceId, String oldStatus, String newStatus, LocalDate lessonDate, LocalTime orarioIngresso, LocalTime orarioUscita) {
        this.attendanceId = attendanceId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.lessonDate = lessonDate;
        this.orarioIngresso = orarioIngresso;
        this.orarioUscita = orarioUscita;
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }

    public LocalTime getOrarioIngresso() { return orarioIngresso; }
    public void setOrarioIngresso(LocalTime orarioIngresso) { this.orarioIngresso = orarioIngresso; }

    public LocalTime getOrarioUscita() { return orarioUscita; }
    public void setOrarioUscita(LocalTime orarioUscita) { this.orarioUscita = orarioUscita; }
} 