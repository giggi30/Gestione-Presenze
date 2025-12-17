package it.unimol.newunimol.attendance_management.service;

import it.unimol.newunimol.attendance_management.event.AttendanceCreatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceUpdatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceDeletedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceStatsGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service responsabile della pubblicazione di eventi RabbitMQ relativi alle presenze.
 * Permette di notificare la creazione, aggiornamento ed eliminazione delle presenze.
 */
@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "RabbitTemplate is a Spring singleton bean, safe to store")
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.attendance}")
    private String attendanceExchange;

    @Value("${rabbitmq.routing.attendance.created}")
    private String attendanceCreatedRouting;

    @Value("${rabbitmq.routing.attendance.updated}")
    private String attendanceUpdatedRouting;

    @Value("${rabbitmq.routing.attendance.deleted}")
    private String attendanceDeletedRouting;

    @Value("${rabbitmq.routing.attendance.stats}")
    private String attendanceStatsRouting;

    public EventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Pubblica un evento di creazione presenza.
     */
    public void publishAttendanceCreated(String attendanceId, String studentId, String courseId,
                                         LocalDate lessonDate, String status, LocalTime orarioIngresso, LocalTime orarioUscita) {
        AttendanceCreatedEvent event = new AttendanceCreatedEvent(
                attendanceId, studentId, courseId, lessonDate, status, orarioIngresso, orarioUscita
        );
        try {
            rabbitTemplate.convertAndSend(attendanceExchange, attendanceCreatedRouting, event);
            logger.info("Published AttendanceCreatedEvent for attendance: {}", attendanceId);
        } catch (Exception e) {
            logger.error("Failed to publish AttendanceCreatedEvent for attendance: {}", attendanceId, e);
        }
    }

    /**
     * Pubblica un evento di aggiornamento presenza.
     */
    public void publishAttendanceUpdated(String attendanceId, String oldStatus, String newStatus,
                                         LocalDate lessonDate, LocalTime orarioIngresso, LocalTime orarioUscita) {
        AttendanceUpdatedEvent event = new AttendanceUpdatedEvent(
                attendanceId, oldStatus, newStatus, lessonDate, orarioIngresso, orarioUscita
        );
        try {
            rabbitTemplate.convertAndSend(attendanceExchange, attendanceUpdatedRouting, event);
            logger.info("Published AttendanceUpdatedEvent for attendance: {}", attendanceId);
        } catch (Exception e) {
            logger.error("Failed to publish AttendanceUpdatedEvent for attendance: {}", attendanceId, e);
        }
    }

    /**
     * Pubblica un evento di eliminazione presenza.
     */
    public void publishAttendanceDeleted(String attendanceId, String studentId, String courseId,
                                         LocalDate lessonDate) {
        AttendanceDeletedEvent event = new AttendanceDeletedEvent(
                attendanceId, studentId, courseId, lessonDate
        );
        try {
            rabbitTemplate.convertAndSend(attendanceExchange, attendanceDeletedRouting, event);
            logger.info("Published AttendanceDeletedEvent for attendance: {}", attendanceId);
        } catch (Exception e) {
            logger.error("Failed to publish AttendanceDeletedEvent for attendance: {}", attendanceId, e);
        }
    }

    /**
     * Pubblica un evento di statistiche generate.
     */
    public void publishAttendanceStatsGenerated(AttendanceStatsGeneratedEvent event) {
        try {
            rabbitTemplate.convertAndSend(attendanceExchange, attendanceStatsRouting, event);
            logger.info("Published AttendanceStatsGeneratedEvent for request: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to publish AttendanceStatsGeneratedEvent for request: {}", event.getRequestId(), e);
        }
    }
}