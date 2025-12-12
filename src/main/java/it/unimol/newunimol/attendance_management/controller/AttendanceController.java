package it.unimol.newunimol.attendance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unimol.newunimol.attendance_management.model.Presenza;
import it.unimol.newunimol.attendance_management.service.AttendanceService;
import it.unimol.newunimol.attendance_management.DTO.AttendanceUpdateDTO;
import it.unimol.newunimol.attendance_management.service.TokenJWTService;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private TokenJWTService tokenJWTService;

    // Endpoint di test
    @GetMapping("/test")
    public String testEndpoint() {
        return "L'applicazione funziona correttamente!";
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null) {
            throw new IllegalArgumentException("Header Authorization mancante");
        }
        authHeader = authHeader.trim();
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization non valido (manca 'Bearer ')");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token JWT mancante nell'header Authorization");
        }
        return token;
    }

    // 1. Registrazione Presenza
    @PostMapping("/createAttendance")
    public ResponseEntity<?> createAttendance(@RequestHeader("Authorization") String authHeader, @RequestBody Presenza presenza) {
        String token = extractTokenFromHeader(authHeader);
        if (!tokenJWTService.hasRole(token, "DOCENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
        }
        return ResponseEntity.ok(attendanceService.createAttendance(presenza));
    }

    // 2. Modifica Presenza
    @PutMapping("/updateAttendance/{attendanceId}")
    public ResponseEntity<?> updateAttendance(@RequestHeader("Authorization") String authHeader, @PathVariable String attendanceId, @RequestBody AttendanceUpdateDTO updateDTO) {
        String token = extractTokenFromHeader(authHeader);
        if (!tokenJWTService.hasRole(token, "DOCENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
        }
        try {
            var updated = attendanceService.updateAttendance(attendanceId, updateDTO);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("attendanceId errato o presenza non trovata");
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // 3. Eliminazione Presenza
    @DeleteMapping("/deleteAttendance/{attendanceId}")
    public ResponseEntity<?> deleteAttendance(@RequestHeader("Authorization") String authHeader, @PathVariable String attendanceId) {
        String token = extractTokenFromHeader(authHeader);
        if (!tokenJWTService.hasRole(token, "DOCENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
        }
        try {
            attendanceService.deleteAttendance(attendanceId);
            return ResponseEntity.ok("Presenza eliminata con successo");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body("attendanceId errato");
        }
    }

    // 4. Visualizzazione Presenze di uno Studente
    @GetMapping("/getStudentAttendances/{studentId}")
    public ResponseEntity<?> getStudentAttendances(@RequestHeader("Authorization") String authHeader, @PathVariable String studentId) {
        String token = extractTokenFromHeader(authHeader);
        String role = tokenJWTService.extractRole(token);
        if (!role.equalsIgnoreCase("DOCENTE") && !role.equalsIgnoreCase("STUDENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo a studenti o docenti");
        }
        return ResponseEntity.ok(attendanceService.getStudentAttendances(studentId));
    }

    // 5. Visualizzazione presenza tramite ID
    @GetMapping("/getAttendance/{attendanceId}")
    public ResponseEntity<?> getAttendanceById(@RequestHeader("Authorization") String authHeader, @PathVariable String attendanceId) {
        String token = extractTokenFromHeader(authHeader);
        String role = tokenJWTService.extractRole(token);
        if (!role.equalsIgnoreCase("DOCENTE") && !role.equalsIgnoreCase("STUDENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo a studenti o docenti");
        }
        return ResponseEntity.ok(attendanceService.getAttendanceByIdDTO(attendanceId));
    }

    // 6. Visualizzazione Presenze per Corso
    @GetMapping("/getCourseAttendances/{courseId}")
    public ResponseEntity<?> getCourseAttendances(@RequestHeader("Authorization") String authHeader, @PathVariable String courseId) {
        String token = extractTokenFromHeader(authHeader);
        String role = tokenJWTService.extractRole(token);
        if (!role.equalsIgnoreCase("DOCENTE") && !role.equalsIgnoreCase("STUDENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo a studenti o docenti");
        }
        return ResponseEntity.ok(attendanceService.getCourseAttendances(courseId));
    }

    // 7. Visualizzazione presenze per giorno
    @GetMapping("/getAttendancesByDay/{date}")
    public ResponseEntity<?> getAttendancesByDay(@RequestHeader("Authorization") String authHeader, @PathVariable LocalDate date) {
        String token = extractTokenFromHeader(authHeader);
        String role = tokenJWTService.extractRole(token);
        if (!role.equalsIgnoreCase("DOCENTE") && !role.equalsIgnoreCase("STUDENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo a studenti o docenti");
        }
        return ResponseEntity.ok(attendanceService.getAttendancesByDay(date));
    }

    // 8. Percentuale presenze di uno studente per un corso
    @GetMapping("/attendances/student/{studentId}/course/{courseId}/attendance-percentage")
    public ResponseEntity<?> getStudentCourseStatistics(@RequestHeader("Authorization") String authHeader, @PathVariable String studentId, @PathVariable String courseId) {
        String token = extractTokenFromHeader(authHeader);
        String role = tokenJWTService.extractRole(token);
        if (!role.equalsIgnoreCase("DOCENTE") && !role.equalsIgnoreCase("STUDENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo a studenti o docenti");
        }
        return ResponseEntity.ok(attendanceService.getStudentCourseStatistics(studentId, courseId));
    }

    // 9. Media delle presenze di tutti gli studenti a un corso
    @GetMapping("/attendances/course/{courseId}/attendance-average")
    public ResponseEntity<?> getCourseStatistics(@RequestHeader("Authorization") String authHeader, @PathVariable String courseId) {
        String token = extractTokenFromHeader(authHeader);
        if (!tokenJWTService.hasRole(token, "DOCENTE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operazione consentita solo ai docenti");
        }
        return ResponseEntity.ok(attendanceService.getCourseStatistics(courseId));
    }

    
}
