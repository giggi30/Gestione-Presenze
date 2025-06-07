package it.unimol.newunimol.attendance_management.service;

import it.unimol.newunimol.attendance_management.event.AttendanceCreatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceUpdatedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceDeletedEvent;
import it.unimol.newunimol.attendance_management.event.ReportRequestedEvent;
import it.unimol.newunimol.attendance_management.event.AttendanceStatsGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EventListenerService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private EventPublisherService eventPublisherService;

    @RabbitListener(queues = "${rabbitmq.queue.attendance.created}")
    public void handleAttendanceCreated(AttendanceCreatedEvent event) {
        logger.info("[RabbitMQ] Ricevuto AttendanceCreatedEvent: attendanceId={}, studentId={}, courseId={}, lessonDate={}, status={}, orarioIngresso={}, orarioUscita={}",
                event.getAttendanceId(), event.getStudentId(), event.getCourseId(), event.getLessonDate(), event.getStatus(), event.getOrarioIngresso(), event.getOrarioUscita());
        // Qui puoi aggiungere logica di business
    }

    @RabbitListener(queues = "${rabbitmq.queue.attendance.updated}")
    public void handleAttendanceUpdated(AttendanceUpdatedEvent event) {
        logger.info("[RabbitMQ] Ricevuto AttendanceUpdatedEvent: attendanceId={}, oldStatus={}, newStatus={}, lessonDate={}, orarioIngresso={}, orarioUscita={}",
                event.getAttendanceId(), event.getOldStatus(), event.getNewStatus(), event.getLessonDate(), event.getOrarioIngresso(), event.getOrarioUscita());
        // Qui puoi aggiungere logica di business
    }

    @RabbitListener(queues = "${rabbitmq.queue.attendance.deleted}")
    public void handleAttendanceDeleted(AttendanceDeletedEvent event) {
        logger.info("[RabbitMQ] Ricevuto AttendanceDeletedEvent: attendanceId={}, studentId={}, courseId={}, lessonDate={}",
                event.getAttendanceId(), event.getStudentId(), event.getCourseId(), event.getLessonDate());
        // Qui puoi aggiungere logica di business
    }

    @RabbitListener(queues = "${rabbitmq.queue.report.requested}")
    public void handleReportRequested(ReportRequestedEvent event) {
        logger.info("[RabbitMQ] Ricevuto ReportRequestedEvent: requestId={}, studentId={}, courseId={}, reportType={}",
                event.getRequestId(), event.getStudentId(), event.getCourseId(), event.getReportType());
        try {
            if ("percentage".equals(event.getReportType())) {
                // Statistiche percentuali per studente su corso
                var stats = attendanceService.getStudentCourseStatistics(event.getStudentId(), event.getCourseId());
                AttendanceStatsGeneratedEvent statsEvent = new AttendanceStatsGeneratedEvent(
                        event.getRequestId(),
                        event.getStudentId(),
                        event.getCourseId(),
                        stats.get("totalCourseLessons"),
                        stats.get("presentLessons"),
                        stats.get("attendancePercentage"),
                        null,
                        LocalDateTime.now()
                );
                eventPublisherService.publishAttendanceStatsGenerated(statsEvent);
            } else if ("average".equals(event.getReportType())) {
                // Statistiche medie per corso
                var stats = attendanceService.getCourseStatistics(event.getCourseId());
                AttendanceStatsGeneratedEvent statsEvent = new AttendanceStatsGeneratedEvent(
                        event.getRequestId(),
                        null,
                        event.getCourseId(),
                        stats.get("totalLessons"),
                        null,
                        null,
                        stats.get("averagePresencesPerLesson"),
                        LocalDateTime.now()
                );
                eventPublisherService.publishAttendanceStatsGenerated(statsEvent);
            }
            logger.info("[RabbitMQ] Statistiche pubblicate per requestId={}", event.getRequestId());
        } catch (Exception e) {
            logger.error("[RabbitMQ] Errore nel processare ReportRequestedEvent: {}", event.getRequestId(), e);
        }
    }
}