package it.unimol.newunimol.attendance_management.service;

import org.springframework.stereotype.Service;

import it.unimol.newunimol.attendance_management.model.presenza;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class AttendanceService {
    private List<presenza> presenze = new ArrayList<>();
    
    //crea una nuova presenza
    public presenza createAttendance(presenza presenza) {
            presenza newPresenza = new presenza(
                UUID.randomUUID().toString(),
                presenza.studentId(),
                presenza.courseId(),
                presenza.lessonDate(),
                presenza.status()
            );
            presenze.add(newPresenza);
            return newPresenza;
    }

    //aggiorna una presenza per id
    public presenza updateAttendance(String attendanceId, presenza presenza) {
        return presenze.stream()
            .filter(p -> p.attendanceId().equals(attendanceId))
            .findFirst()
            .map(p -> {
                presenza updatedPresenza = new presenza(
                    p.attendanceId(),
                    p.studentId(),
                    p.courseId(),
                    p.lessonDate(),
                    presenza.status()
                );
                presenze.set(presenze.indexOf(p), updatedPresenza);
                return updatedPresenza;
            })
            .orElse(null);
    }

    //elimina una presenza per id
    public void deleteAttendance(String attendanceId) {
        presenze.removeIf(p -> p.attendanceId().equals(attendanceId));
    }

    //filtra le presenze per un determinato id presenza
    public presenza getAttendanceById(String attendanceId) {
        return presenze.stream()
            .filter(p -> p.attendanceId().equals(attendanceId))
            .findFirst()
            .orElse(null);
    }

    //filtra le presenze per un determinato studente
    public List<presenza> getStudentAttendances(String studentId) {
        return presenze.stream()
            .filter(p -> p.studentId().equals(studentId))
            .collect(Collectors.toList());
    }

    //filtra le presenze per un determinato corso
    public List<presenza> getCourseAttendances(String courseId) {
        return presenze.stream()
            .filter(p -> p.courseId().equals(courseId))
            .collect(Collectors.toList());
    }


    //calcola le statistiche di uno studente per un determinato corso
    public Map<String, Double> getStudentCourseStatistics(String studentId, String courseId) {
        // Ottiene tutte le presenze dello studente per quel corso
        List<presenza> studentCourseAttendances = presenze.stream()
            .filter(p -> p.studentId().equals(studentId) && p.courseId().equals(courseId))
            .collect(Collectors.toList());

        // Ottiene il numero totale di lezioni del corso
        long totalCourseLessons = presenze.stream()
            .filter(p -> p.courseId().equals(courseId))
            .map(p -> p.lessonDate())
            .distinct()
            .count();

        // Conta le presenze effettive dello studente
        long presentLessons = studentCourseAttendances.stream()
            .filter(p -> "present".equals(p.status()))
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
    


    //media delle presenze di tutti gli studenti ad un corso 
    public Map<String, Double> getCourseStatistics(String courseId) {
        // Ottiene tutte le date delle lezioni del corso
        List<LocalDate> courseDates = presenze.stream()
            .filter(p -> p.courseId().equals(courseId))
            .map(p -> p.lessonDate())
            .distinct()
            .collect(Collectors.toList());

        // Per ogni lezione, conta il numero di presenze
        List<Long> presencesPerLesson = courseDates.stream()
            .map(date -> presenze.stream()
                .filter(p -> p.courseId().equals(courseId) && 
                       p.lessonDate().equals(date) && 
                       "present".equals(p.status()))
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
}
