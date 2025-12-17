package it.unimol.newunimol.attendance_management.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import it.unimol.newunimol.attendance_management.event.AttendanceCreatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceDeletedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceStatsGeneratedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceUpdatedEvent;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisherService eventPublisherService;

    private final String exchange = "test.exchange";
    private final String createdRouting = "test.created";
    private final String updatedRouting = "test.updated";
    private final String deletedRouting = "test.deleted";
    private final String statsRouting = "test.stats";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventPublisherService, "attendanceExchange", exchange);
        ReflectionTestUtils.setField(eventPublisherService, "attendanceCreatedRouting", createdRouting);
        ReflectionTestUtils.setField(eventPublisherService, "attendanceUpdatedRouting", updatedRouting);
        ReflectionTestUtils.setField(eventPublisherService, "attendanceDeletedRouting", deletedRouting);
        ReflectionTestUtils.setField(eventPublisherService, "attendanceStatsRouting", statsRouting);
    }

    @Test
    void publishAttendanceCreated_ShouldSendEvent() {
        // Arrange
        String attendanceId = "1";
        String studentId = "s1";
        String courseId = "c1";
        LocalDate date = LocalDate.now();
        String status = "PRESENT";
        LocalTime in = LocalTime.of(9, 0);
        LocalTime out = LocalTime.of(11, 0);

        // Act
        eventPublisherService.publishAttendanceCreated(attendanceId, studentId, courseId, date, status, in, out);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(createdRouting), any(AttendanceCreatedEvent.class));
    }

    @Test
    void publishAttendanceUpdated_ShouldSendEvent() {
        // Arrange
        String attendanceId = "1";
        String oldStatus = "ABSENT";
        String newStatus = "PRESENT";
        LocalDate date = LocalDate.now();
        LocalTime in = LocalTime.of(9, 0);
        LocalTime out = LocalTime.of(11, 0);

        // Act
        eventPublisherService.publishAttendanceUpdated(attendanceId, oldStatus, newStatus, date, in, out);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(updatedRouting), any(AttendanceUpdatedEvent.class));
    }

    @Test
    void publishAttendanceDeleted_ShouldSendEvent() {
        // Arrange
        String attendanceId = "1";
        String studentId = "s1";
        String courseId = "c1";
        LocalDate date = LocalDate.now();

        // Act
        eventPublisherService.publishAttendanceDeleted(attendanceId, studentId, courseId, date);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(deletedRouting), any(AttendanceDeletedEvent.class));
    }

    @Test
    void publishAttendanceStatsGenerated_ShouldSendEvent() {
        // Arrange
        AttendanceStatsGeneratedEvent event = new AttendanceStatsGeneratedEvent();
        event.setRequestId("req-1");

        // Act
        eventPublisherService.publishAttendanceStatsGenerated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(statsRouting), eq(event));
    }
}
