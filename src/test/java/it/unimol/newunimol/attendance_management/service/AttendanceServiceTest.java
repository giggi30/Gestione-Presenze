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
}