package it.unimol.newunimol.attendance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import it.unimol.newunimol.attendance_management.model.presenza;
import it.unimol.newunimol.attendance_management.service.AttendanceService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Endpoint di test
    @GetMapping("/test")
    public String testEndpoint() {
        return "L'applicazione funziona correttamente!";
    }

    // 1. Registrazione Presenza
    @PostMapping("/attendances")
    public presenza createAttendance(@RequestBody presenza presenza) {
        return attendanceService.createAttendance(presenza);
    }

    // 2. Modifica Presenza
    @PutMapping("/attendances/{attendanceId}")
    public presenza updateAttendance(@PathVariable String attendanceId, @RequestBody presenza presenza) {
        return attendanceService.updateAttendance(attendanceId, presenza);
    }

    // 3. Eliminazione Presenza
    @DeleteMapping("/attendances/{attendanceId}")
    public void deleteAttendance(@PathVariable String attendanceId) {
        attendanceService.deleteAttendance(attendanceId);
    }

    // 4. Visualizzazione Presenze di uno Studente
    @GetMapping("/attendances/student/{studentId}")
    public List<presenza> getStudentAttendances(@PathVariable String studentId) {
        return attendanceService.getStudentAttendances(studentId);
    }

    // 5. Visualizzazione Presenze per Corso
    @GetMapping("/attendances/course/{courseId}")
    public List<presenza> getCourseAttendances(@PathVariable String courseId) {
        return attendanceService.getCourseAttendances(courseId);
    }

    // 6. Percentuale presenze di uno studente per un corso
    @GetMapping("/attendances/student/{studentId}/course/{courseId}/attendance-percentage")
    public Map<String, Double> getStudentCourseStatistics(
            @PathVariable String studentId,
            @PathVariable String courseId) {
        return attendanceService.getStudentCourseStatistics(studentId, courseId);
    }

    // 7. Media delle presenze di tutti gli studenti a un corso
    @GetMapping("/attendances/course/{courseId}/attendance-average")
    public Map<String, Double> getCourseStatistics(@PathVariable String courseId) {
        return attendanceService.getCourseStatistics(courseId);
    }
}
