package it.unimol.newunimol.attendance_management.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unimol.newunimol.attendance_management.event.AttendanceCreatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceUpdatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceDeletedEvent;
import it.unimol.newunimol.attendance_management.event.ReportRequestedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceStatsGeneratedEvent;

@ExtendWith(MockitoExtension.class)
class EventListenerServiceTest {

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private EventListenerService eventListenerService;

    @Test
    void handleAttendanceCreated_ShouldLogEvent() {
        // Arrange
        AttendanceCreatedEvent event = new AttendanceCreatedEvent(
            "att-1", "student-1", "course-1", LocalDate.now(), "present", LocalTime.of(9, 0), null
        );

        // Act
        eventListenerService.handleAttendanceCreated(event);

        // Assert - nessuna eccezione, verifica che il metodo sia stato chiamato
        verify(attendanceService, times(0)).createAttendance(any());
    }

    @Test
    void handleAttendanceUpdated_ShouldLogEvent() {
        // Arrange
        AttendanceUpdatedEvent event = new AttendanceUpdatedEvent(
            "att-1", "absent", "present", LocalDate.now(), LocalTime.of(9, 0), null
        );

        // Act
        eventListenerService.handleAttendanceUpdated(event);

        // Assert - nessuna eccezione
        verify(attendanceService, times(0)).updateAttendance(any(), any());
    }

    @Test
    void handleAttendanceDeleted_ShouldLogEvent() {
        // Arrange
        AttendanceDeletedEvent event = new AttendanceDeletedEvent(
            "att-1", "student-1", "course-1", LocalDate.now()
        );

        // Act
        eventListenerService.handleAttendanceDeleted(event);

        // Assert - nessuna eccezione
        verify(attendanceService, times(0)).deleteAttendance(any());
    }

    @Test
    void handleReportRequested_PercentageType_ShouldPublishStats() {
        // Arrange
        ReportRequestedEvent event = new ReportRequestedEvent(
            "req-1", "student-1", "course-1", "percentage"
        );
        
        Map<String, Double> stats = Map.of(
            "totalCourseLessons", 10.0,
            "presentLessons", 8.0,
            "attendancePercentage", 80.0
        );
        
        when(attendanceService.getStudentCourseStatistics("student-1", "course-1")).thenReturn(stats);

        // Act
        eventListenerService.handleReportRequested(event);

        // Assert
        verify(attendanceService).getStudentCourseStatistics("student-1", "course-1");
        verify(eventPublisherService).publishAttendanceStatsGenerated(any(AttendanceStatsGeneratedEvent.class));
    }

    @Test
    void handleReportRequested_AverageType_ShouldPublishStats() {
        // Arrange
        ReportRequestedEvent event = new ReportRequestedEvent(
            "req-2", null, "course-1", "average"
        );
        
        Map<String, Double> stats = Map.of(
            "totalLessons", 10.0,
            "averagePresencesPerLesson", 25.5
        );
        
        when(attendanceService.getCourseStatistics("course-1")).thenReturn(stats);

        // Act
        eventListenerService.handleReportRequested(event);

        // Assert
        verify(attendanceService).getCourseStatistics("course-1");
        verify(eventPublisherService).publishAttendanceStatsGenerated(any(AttendanceStatsGeneratedEvent.class));
    }

    @Test
    void handleReportRequested_UnknownType_ShouldNotPublishStats() {
        // Arrange
        ReportRequestedEvent event = new ReportRequestedEvent(
            "req-3", "student-1", "course-1", "unknown"
        );

        // Act
        eventListenerService.handleReportRequested(event);

        // Assert - no stats should be calculated or published
        verify(attendanceService, times(0)).getStudentCourseStatistics(any(), any());
        verify(attendanceService, times(0)).getCourseStatistics(any());
        verify(eventPublisherService, times(0)).publishAttendanceStatsGenerated(any());
    }

    @Test
    void handleReportRequested_Exception_ShouldLogError() {
        // Arrange
        ReportRequestedEvent event = new ReportRequestedEvent(
            "req-4", "student-1", "course-1", "percentage"
        );
        
        when(attendanceService.getStudentCourseStatistics("student-1", "course-1"))
            .thenThrow(new RuntimeException("Database error"));

        // Act - non dovrebbe sollevare eccezione
        eventListenerService.handleReportRequested(event);

        // Assert - l'errore viene catturato e loggato
        verify(attendanceService).getStudentCourseStatistics("student-1", "course-1");
        verify(eventPublisherService, times(0)).publishAttendanceStatsGenerated(any());
    }
}
