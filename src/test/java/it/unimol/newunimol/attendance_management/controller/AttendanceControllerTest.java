package it.unimol.newunimol.attendance_management.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimol.newunimol.attendance_management.model.Presenza;
import it.unimol.newunimol.attendance_management.service.AttendanceService;
import it.unimol.newunimol.attendance_management.service.TokenJWTService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AttendanceControllerTest.TestConfig.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private TokenJWTService tokenJWTService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AttendanceService attendanceService() {
            return mock(AttendanceService.class);
        }

        @Bean
        @Primary
        public TokenJWTService tokenJWTService() {
            return mock(TokenJWTService.class);
        }
    }

    @Test
    void testEndpoint_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("L'applicazione funziona correttamente!"));
    }

    @Test
    void createAttendance_WithDocenteRole_ShouldReturnOk() throws Exception {
        String token = "valid-token";
        String authHeader = "Bearer " + token;
        
        Presenza inputPresenza = new Presenza();
        inputPresenza.setStudentId("123");
        inputPresenza.setCourseId("CS101");
        inputPresenza.setLessonDate(LocalDate.now());
        inputPresenza.setOrarioIngresso(LocalTime.of(9, 0));
        inputPresenza.setStatus("PRESENT");

        Presenza savedPresenza = new Presenza(
            "id-1", "123", "CS101", LocalDate.now(), "PRESENT", LocalTime.of(9, 0), null
        );

        when(tokenJWTService.hasRole(token, "DOCENTE")).thenReturn(true);
        when(attendanceService.createAttendance(any(Presenza.class))).thenReturn(savedPresenza);

        // Act & Assert
        mockMvc.perform(post("/api/createAttendance")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPresenza)))
                .andExpect(status().isOk());
    }

    @Test
    void createAttendance_WithStudentRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        String token = "student-token";
        String authHeader = "Bearer " + token;
        
        Presenza inputPresenza = new Presenza();
        inputPresenza.setStudentId("123");

        when(tokenJWTService.hasRole(token, "DOCENTE")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/createAttendance")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPresenza)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAttendance_WithoutToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Presenza inputPresenza = new Presenza();

        // Act & Assert
        mockMvc.perform(post("/api/createAttendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPresenza)))
                .andExpect(status().isBadRequest());
    }
}
