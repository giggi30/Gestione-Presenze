package it.unimol.newunimol.attendance_management.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unimol.newunimol.attendance_management.model.presenza;
import it.unimol.newunimol.attendance_management.repository.PresenzaRepository;
import it.unimol.newunimol.attendance_management.DTO.AttendanceUpdateDTO;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private PresenzaRepository presenzaRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void createAttendanceTest() {
        // Arrange
        presenza inputPresenza = new presenza();
        inputPresenza.setStudentId("123");
        inputPresenza.setCourseId("CS101");
        inputPresenza.setLessonDate(LocalDate.of(2024, 3, 20));
        inputPresenza.setStatus("PRESENT");
        inputPresenza.setOrarioIngresso(LocalTime.of(9, 0));

        // Simuliamo l'oggetto salvato dal repository (che avrà un ID generato)
        presenza savedPresenza = new presenza(
            "generated-id-123",
            "123",
            "CS101",
            LocalDate.of(2024, 3, 20),
            "PRESENT",
            LocalTime.of(9, 0),
            null
        );

        when(presenzaRepository.save(any(presenza.class))).thenReturn(savedPresenza);

        // Act
        presenza result = attendanceService.createAttendance(inputPresenza);

        // Assert
        assertNotNull(result);
        assertEquals("generated-id-123", result.getAttendanceId());
        assertEquals("123", result.getStudentId());

        // Verifichiamo che il repository sia stato chiamato
        verify(presenzaRepository).save(any(presenza.class));
        
        // Verifichiamo che l'evento sia stato pubblicato
        verify(eventPublisherService).publishAttendanceCreated(
            "generated-id-123", 
            "123", 
            "CS101", 
            LocalDate.of(2024, 3, 20), 
            "PRESENT", 
            LocalTime.of(9, 0), 
            null
        );
    }

    @Test
    void updateAttendance_ShouldUpdateAndPublishEvent() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "absent", null, null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO("present", LocalTime.of(9, 30), null);
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));
        when(presenzaRepository.save(any(presenza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("present", result.getStatus());
        assertEquals(LocalTime.of(9, 30), result.getOrarioIngresso());
        
        verify(eventPublisherService).publishAttendanceUpdated(
            eq(attendanceId), eq("absent"), eq("present"), any(LocalDate.class), eq(LocalTime.of(9, 30)), isNull()
        );
    }

    @Test
    void updateAttendance_WithInvalidLogic_ShouldThrowException() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "present", LocalTime.of(9, 0), null
        );
        
        // Tentativo di segnare entrata in ritardo su uno stato "present" (non "absent")
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO(null, LocalTime.of(9, 30), null);
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.updateAttendance(attendanceId, updateDTO);
        });
    }

    @Test
    void deleteAttendance_ShouldDeleteAndPublishEvent() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "present", LocalTime.of(9, 0), null
        );
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));

        // Act
        attendanceService.deleteAttendance(attendanceId);

        // Assert
        verify(presenzaRepository).deleteById(attendanceId);
        verify(eventPublisherService).publishAttendanceDeleted(
            eq(attendanceId), eq("student-1"), eq("course-1"), any(LocalDate.class)
        );
    }

    @Test
    void deleteAttendance_NotFound_ShouldThrowException() {
        // Arrange
        String attendanceId = "id-123";
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            attendanceService.deleteAttendance(attendanceId);
        });
    }

    @Test
    void getStudentAttendances_ShouldReturnList() {
        // Arrange
        String studentId = "student-1";
        when(presenzaRepository.findByStudentId(studentId)).thenReturn(java.util.List.of(new presenza()));

        // Act
        var result = attendanceService.getStudentAttendances(studentId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void updateAttendance_EarlyExit_Success() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "present", LocalTime.of(9, 0), null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO(null, null, LocalTime.of(11, 0));
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));
        when(presenzaRepository.save(any(presenza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(11, 0), result.getOrarioUscita());
    }

    @Test
    void updateAttendance_EarlyExit_Failure() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "absent", null, null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO(null, null, LocalTime.of(11, 0));
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.updateAttendance(attendanceId, updateDTO);
        });
    }

    @Test
    void updateAttendance_NotFound() {
        // Arrange
        String attendanceId = "id-missing";
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO("present", null, null);
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.empty());

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNull(result);
    }

    @Test
    void getAttendanceById_Found() {
        // Arrange
        String attendanceId = "id-123";
        presenza p = new presenza();
        p.setAttendanceId(attendanceId);
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(p));

        // Act
        presenza result = attendanceService.getAttendanceById(attendanceId);

        // Assert
        assertNotNull(result);
        assertEquals(attendanceId, result.getAttendanceId());
    }

    @Test
    void getAttendanceById_NotFound() {
        // Arrange
        String attendanceId = "id-missing";
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.empty());

        // Act
        presenza result = attendanceService.getAttendanceById(attendanceId);

        // Assert
        assertNull(result);
    }

    @Test
    void getCourseAttendances_ShouldReturnList() {
        // Arrange
        String courseId = "course-1";
        when(presenzaRepository.findByCourseId(courseId)).thenReturn(java.util.List.of(new presenza()));

        // Act
        var result = attendanceService.getCourseAttendances(courseId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAttendancesByDay_ShouldReturnList() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(presenzaRepository.findByLessonDate(date)).thenReturn(java.util.List.of(new presenza()));

        // Act
        var result = attendanceService.getAttendancesByDay(date);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getStudentCourseStatistics_ShouldCalculateCorrectly() {
        // Arrange
        String studentId = "student-1";
        String courseId = "course-1";
        
        presenza p1 = new presenza("1", studentId, courseId, LocalDate.of(2024, 1, 1), "present", null, null);
        presenza p2 = new presenza("2", studentId, courseId, LocalDate.of(2024, 1, 2), "absent", null, null);
        
        // Mock findByStudentId (returns all attendances for student)
        when(presenzaRepository.findByStudentId(studentId)).thenReturn(java.util.List.of(p1, p2));
        
        // Mock findByCourseId (returns all attendances for course to calculate total lessons)
        // Assuming 2 lessons total
        when(presenzaRepository.findByCourseId(courseId)).thenReturn(java.util.List.of(p1, p2));

        // Act
        var stats = attendanceService.getStudentCourseStatistics(studentId, courseId);

        // Assert
        assertEquals(2.0, stats.get("totalCourseLessons"));
        assertEquals(1.0, stats.get("presentLessons"));
        assertEquals(50.0, stats.get("attendancePercentage"));
    }

    @Test
    void getStudentCourseStatistics_NoLessons_ShouldReturnZero() {
        // Arrange
        String studentId = "student-1";
        String courseId = "course-empty";
        
        when(presenzaRepository.findByStudentId(studentId)).thenReturn(java.util.Collections.emptyList());
        when(presenzaRepository.findByCourseId(courseId)).thenReturn(java.util.Collections.emptyList());

        // Act
        var stats = attendanceService.getStudentCourseStatistics(studentId, courseId);

        // Assert
        assertEquals(0.0, stats.get("totalCourseLessons"));
        assertEquals(0.0, stats.get("presentLessons"));
        assertEquals(0.0, stats.get("attendancePercentage"));
    }

    @Test
    void getCourseStatistics_ShouldCalculateAverage() {
        // Arrange
        String courseId = "course-1";
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 1, 2);
        
        // Lesson 1: 2 students present
        presenza p1 = new presenza("1", "s1", courseId, date1, "present", null, null);
        presenza p2 = new presenza("2", "s2", courseId, date1, "present", null, null);
        
        // Lesson 2: 1 student present, 1 absent
        presenza p3 = new presenza("3", "s1", courseId, date2, "present", null, null);
        presenza p4 = new presenza("4", "s2", courseId, date2, "absent", null, null);
        
        when(presenzaRepository.findByCourseId(courseId)).thenReturn(java.util.List.of(p1, p2, p3, p4));

        // Act
        var stats = attendanceService.getCourseStatistics(courseId);

        // Assert
        assertEquals(2.0, stats.get("totalLessons")); // 2 distinct dates
        // Lesson 1: 2 presences. Lesson 2: 1 presence. Average: (2+1)/2 = 1.5
        assertEquals(1.5, stats.get("averagePresencesPerLesson"));
    }

    @Test
    void getCourseStatistics_NoLessons_ShouldReturnZero() {
        // Arrange
        String courseId = "course-empty";
        when(presenzaRepository.findByCourseId(courseId)).thenReturn(java.util.Collections.emptyList());

        // Act
        var stats = attendanceService.getCourseStatistics(courseId);

        // Assert
        assertEquals(0.0, stats.get("totalLessons"));
        assertEquals(0.0, stats.get("averagePresencesPerLesson"));
    }

    @Test
    void getAttendanceByIdDTO_Found() {
        // Arrange
        String attendanceId = "id-123";
        presenza p = new presenza(attendanceId, "s1", "c1", LocalDate.now(), "present", null, null);
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(p));

        // Act
        var dto = attendanceService.getAttendanceByIdDTO(attendanceId);

        // Assert
        assertNotNull(dto);
        assertEquals(attendanceId, dto.attendanceId());
    }

    @Test
    void getAttendanceByIdDTO_NotFound() {
        // Arrange
        String attendanceId = "id-missing";
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.empty());

        // Act
        var dto = attendanceService.getAttendanceByIdDTO(attendanceId);

        // Assert
        assertNull(dto);
    }

    @Test
    void updateAttendance_UpdateStatusOnly() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "absent", null, null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO("present", null, null);
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));
        when(presenzaRepository.save(any(presenza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("present", result.getStatus());
        assertNull(result.getOrarioIngresso());
        assertNull(result.getOrarioUscita());
    }

    @Test
    void updateAttendance_UpdateOrarioIngressoOnly() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "absent", null, null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO(null, LocalTime.of(9, 30), null);
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));
        when(presenzaRepository.save(any(presenza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("absent", result.getStatus()); // Status unchanged
        assertEquals(LocalTime.of(9, 30), result.getOrarioIngresso());
    }

    @Test
    void updateAttendance_UpdateOrarioUscitaOnly() {
        // Arrange
        String attendanceId = "id-123";
        presenza existingPresenza = new presenza(
            attendanceId, "student-1", "course-1", LocalDate.now(), "present", LocalTime.of(9, 0), null
        );
        
        AttendanceUpdateDTO updateDTO = new AttendanceUpdateDTO(null, null, LocalTime.of(11, 0));
        
        when(presenzaRepository.findById(attendanceId)).thenReturn(java.util.Optional.of(existingPresenza));
        when(presenzaRepository.save(any(presenza.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        presenza result = attendanceService.updateAttendance(attendanceId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("present", result.getStatus()); // Status unchanged
        assertEquals(LocalTime.of(11, 0), result.getOrarioUscita());
    }
}