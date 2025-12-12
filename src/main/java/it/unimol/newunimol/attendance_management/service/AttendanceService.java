package it.unimol.newunimol.attendance_management.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import it.unimol.newunimol.attendance_management.model.Presenza;
import it.unimol.newunimol.attendance_management.repository.PresenzaRepository;
import it.unimol.newunimol.attendance_management.DTO.AttendanceUpdateDTO;
import it.unimol.newunimol.attendance_management.DTO.AttendanceDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

/**
 * Service per la gestione delle presenze degli studenti ai corsi.
 * Fornisce metodi per creare, aggiornare, eliminare e ottenere statistiche sulle presenze.
 */
@Service
public class AttendanceService {
    @Autowired
    private PresenzaRepository presenzaRepository;
    
    @Autowired
    private EventPublisherService eventPublisherService;
    
    /**
     * Crea una nuova presenza.
     * @param presenza la presenza da aggiungere
     * @return la presenza creata con un nuovo ID
     */
    public Presenza createAttendance(Presenza presenza) {
        Presenza newPresenza = new Presenza(
            UUID.randomUUID().toString(),
            presenza.getStudentId(),
            presenza.getCourseId(),
            presenza.getLessonDate(),
            presenza.getStatus(),
            presenza.getOrarioIngresso(),
            null // orarioUscita sempre null in creazione
        );
        Presenza saved = presenzaRepository.save(newPresenza);
        // Pubblica evento RabbitMQ
        eventPublisherService.publishAttendanceCreated(
            saved.getAttendanceId(),
            saved.getStudentId(),
            saved.getCourseId(),
            saved.getLessonDate(),
            saved.getStatus(),
            saved.getOrarioIngresso(),
            saved.getOrarioUscita()
        );
        return saved;
    }

    /**
     * Aggiorna una presenza esistente tramite ID.
     * @param attendanceId l'ID della presenza da aggiornare
     * @param updateDTO il DTO con i nuovi valori
     * @return la presenza aggiornata, o null se non trovata
     */
    public Presenza updateAttendance(String attendanceId, AttendanceUpdateDTO updateDTO) {
        return presenzaRepository.findById(attendanceId)
            .map(p -> {
                String oldStatus = p.getStatus();
                // Controllo coerenza logica
                if (updateDTO.orarioIngresso() != null) {
                    // Entrata in ritardo: solo se lo stato attuale è "absent"
                    if (!"absent".equalsIgnoreCase(oldStatus)) {
                        throw new IllegalArgumentException("Entrata in ritardo consentita solo su assenze");
                    }
                }
                if (updateDTO.orarioUscita() != null) {
                    // Uscita anticipata: solo se lo stato attuale è "present"
                    if (!"present".equalsIgnoreCase(oldStatus)) {
                        throw new IllegalArgumentException("Uscita anticipata consentita solo su presenze");
                    }
                }
                if (updateDTO.status() != null) {
                    p.setStatus(updateDTO.status());
                }
                if (updateDTO.orarioIngresso() != null) {
                    p.setOrarioIngresso(updateDTO.orarioIngresso());
                }
                if (updateDTO.orarioUscita() != null) {
                    p.setOrarioUscita(updateDTO.orarioUscita());
                }
                Presenza updated = presenzaRepository.save(p);
                // Pubblica evento RabbitMQ
                eventPublisherService.publishAttendanceUpdated(
                    updated.getAttendanceId(),
                    oldStatus,
                    updated.getStatus(),
                    updated.getLessonDate(),
                    updated.getOrarioIngresso(),
                    updated.getOrarioUscita()
                );
                return updated;
            })
            .orElse(null);
    }

    /**
     * Elimina una presenza tramite ID.
     * @param attendanceId l'ID della presenza da eliminare
     */
    public void deleteAttendance(String attendanceId) {
        Presenza p = presenzaRepository.findById(attendanceId).orElse(null);
        if (p != null) {
            presenzaRepository.deleteById(attendanceId);
            // Pubblica evento RabbitMQ
            eventPublisherService.publishAttendanceDeleted(
                p.getAttendanceId(),
                p.getStudentId(),
                p.getCourseId(),
                p.getLessonDate()
            );
        } else {
            throw new RuntimeException("Presenza non trovata");
        }
    }

    /**
     * Restituisce una presenza tramite ID.
     * @param attendanceId l'ID della presenza
     * @return la presenza trovata, o null se non esiste
     */
    public Presenza getAttendanceById(String attendanceId) {
        return presenzaRepository.findById(attendanceId).orElse(null);
    }

    /**
     * Restituisce tutte le presenze di uno studente.
     * @param studentId l'ID dello studente
     * @return lista delle presenze dello studente
     */
    public List<Presenza> getStudentAttendances(String studentId) {
        return presenzaRepository.findByStudentId(studentId);
    }

    /**
     * Restituisce tutte le presenze di un corso.
     * @param courseId l'ID del corso
     * @return lista delle presenze del corso
     */
    public List<Presenza> getCourseAttendances(String courseId) {
        return presenzaRepository.findByCourseId(courseId);
    }

    /**
     * Restituisce tutte le presenze di un giorno.
     * @param date la data delle presenze
     * @return lista delle presenze del giorno
     */
    public List<Presenza> getAttendancesByDay(LocalDate date) {
        return presenzaRepository.findByLessonDate(date);
    }

    /**
     * Calcola le statistiche di presenza di uno studente per un corso.
     * @param studentId l'ID dello studente
     * @param courseId l'ID del corso
     * @return mappa con totale lezioni, presenze e percentuale
     */
    public Map<String, Double> getStudentCourseStatistics(String studentId, String courseId) {
        // Ottiene tutte le presenze dello studente per quel corso
        List<Presenza> studentCourseAttendances = presenzaRepository.findByStudentId(studentId)
            .stream()
            .filter(p -> p.getCourseId().equals(courseId))
            .toList();

        // Ottiene il numero totale di lezioni del corso
        List<Presenza> courseAttendances = presenzaRepository.findByCourseId(courseId);
        long totalCourseLessons = courseAttendances.stream()
            .map(p -> p.getLessonDate())
            .distinct()
            .count();

        // Conta le presenze effettive dello studente
        long presentLessons = studentCourseAttendances.stream()
            .filter(p -> "present".equals(p.getStatus()))
            .count();

        // Calcola la percentuale di presenze rispetto al totale delle lezioni del corso
        double attendancePercentage;
        if (totalCourseLessons > 0) {
            attendancePercentage = ((double) presentLessons / totalCourseLessons) * 100;
        } else {
            attendancePercentage = 0.0;
        }

        return Map.of(
            "totalCourseLessons", (double) totalCourseLessons,
            "presentLessons", (double) presentLessons,
            "attendancePercentage", attendancePercentage
        );
    }
    
    /**
     * Calcola la media delle presenze per le lezioni di un corso.
     * @param courseId l'ID del corso
     * @return mappa con totale lezioni e media presenze per lezione
     */
    public Map<String, Double> getCourseStatistics(String courseId) {
        // Ottiene tutte le date delle lezioni del corso
        List<Presenza> courseAttendances = presenzaRepository.findByCourseId(courseId);
        List<LocalDate> courseDates = courseAttendances.stream()
            .map(p -> p.getLessonDate())
            .distinct()
            .collect(Collectors.toList());

        // Per ogni lezione, conta il numero di presenze
        List<Long> presencesPerLesson = courseDates.stream()
            .map(date -> courseAttendances.stream()
                .filter(p -> p.getLessonDate().equals(date) && 
                       "present".equals(p.getStatus()))
                .count())
            .collect(Collectors.toList());

        // Calcola la media delle presenze per lezione
        double averagePresencesPerLesson = presencesPerLesson.isEmpty() ? 0.0 :
            presencesPerLesson.stream()
                .mapToDouble(Long::doubleValue)
                .average()
                .orElse(0.0);

        return Map.of(
            "totalLessons", (double) courseDates.size(),
            "averagePresencesPerLesson", averagePresencesPerLesson
        );
    }

    public AttendanceDTO getAttendanceByIdDTO(String attendanceId) {
        Presenza p = getAttendanceById(attendanceId);
        if (p == null) return null;
        return new AttendanceDTO(
            p.getAttendanceId(),
            p.getStudentId(),
            p.getCourseId(),
            p.getLessonDate(),
            p.getStatus(),
            p.getOrarioIngresso(),
            p.getOrarioUscita()
        );
    }
}
