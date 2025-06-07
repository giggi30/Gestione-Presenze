package it.unimol.newunimol.attendance_management.repository;

import it.unimol.newunimol.attendance_management.model.presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface PresenzaRepository extends JpaRepository<presenza, String> {
    List<presenza> findByStudentId(String studentId);
    List<presenza> findByCourseId(String courseId);
    List<presenza> findByLessonDate(LocalDate date);
} 