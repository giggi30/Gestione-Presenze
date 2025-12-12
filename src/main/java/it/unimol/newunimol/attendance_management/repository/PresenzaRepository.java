package it.unimol.newunimol.attendance_management.repository;

import it.unimol.newunimol.attendance_management.model.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface PresenzaRepository extends JpaRepository<Presenza, String> {
    List<Presenza> findByStudentId(String studentId);
    List<Presenza> findByCourseId(String courseId);
    List<Presenza> findByLessonDate(LocalDate date);
} 