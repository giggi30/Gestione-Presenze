package it.unimol.newunimol.attendance_management.event;

import java.io.Serializable;

public class ReportRequestedEvent implements Serializable {
    private String requestId;
    private String studentId; // può essere null per media corso
    private String courseId;
    private String reportType; // "percentage" o "average"

    public ReportRequestedEvent() {}

    public ReportRequestedEvent(String requestId, String studentId, String courseId, String reportType) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.reportType = reportType;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
} 