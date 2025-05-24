# Attendance Management Microservice API

## Descrizione

Questo microservizio si occupa della registrazione, modifica, visualizzazione e analisi delle presenze degli studenti alle lezioni universitarie. È parte del sistema distribuito "NewUnimol" e comunica con altri microservizi come Course_Management, User_Roles_Management e Report_Management. Si può visionare il Progetto tramite Swagger scaricando il file 'attendance-management-stub.yaml', importandolo nel sito [SwaggerEditor] (https://editor.swagger.io/), oppure copiando il raw code di 'attendance-management-stub.yaml' e incollarlo su https://editor.swagger.io/.

---

## Funzionalità Principali

- Registrazione delle presenze degli studenti alle lezioni
- Modifica dello stato di presenza (presente/assente)
- Eliminazione delle registrazioni di presenze
- Visualizzazione delle presenze di un singolo studente
- Visualizzazione delle presenze di uno studente durante un corso
- Calcolo della percentuale di presenze per studente in un corso
- Calcolo della media delle presenze per corso
- Integrazione con altri microservizi per la validazione dei dati
- Gestione delle autorizzazioni basata sui ruoli

---

## Tech Stack

- **Linguaggio di Programmazione:** Java 17
- **Framework:** Spring Boot 3.5.0
- **Database:** PostgreSQL
- **API Documentation:** Swagger
- **Message Broker:** RabbitMQ (per la comunicazione asincrona)

---

## Architettura Database

### Tabella: presenza

| Colonna | Tipo | Descrizione | Vincoli |
|---------|------|-------------|----------|
| attendanceId | String | Identificatore univoco della presenza | PRIMARY KEY |
| studentId | String | ID dello studente | FOREIGN KEY, NOT NULL |
| courseId | String | ID del corso | FOREIGN KEY, NOT NULL |
| lessonDate | LocalDate | Data della lezione | NOT NULL |
| status | String | Stato della presenza (present/absent) | NOT NULL |

**Indici:**
- Indice primario su `attendanceId`
- Indice su `studentId`
- Indice su `courseId`
- Indice composito su `(studentId, courseId, lessonDate)`

---
## DTO
DataTransferObject presenti nel microservizio.

### DTO Presenza

```java
#############################################
# AttendanceDTO
# @desc: DTO per rappresentare una presenza
#############################################
public class AttendanceDTO {
    private String attendanceId;
    private String studentId;
    private String courseId;
    private LocalDate lessonDate;
    private String status; // "present" o "absent"
}
```

### DTO aggiorna Presenza
```java
#############################################
# AttendanceUpdateDTO
# @desc: DTO per aggiornare lo stato di una presenza
#############################################
public class AttendanceUpdateDTO {
    private String status; // "present" o "absent"
}
```

### DTO percentuale Presenze
```java
#############################################
# AttendanceStatsDTO
# @desc: DTO per il calcolo della percentuale di presenze
#############################################
public class AttendanceStatsDTO {
    private double totalCourseLessons;
    private double presentLessons;
    private double attendancePercentage;
}
```

### DTO media Presenze
```java
#############################################
# CourseAttendanceStatsDTO
# @desc: DTO per la media delle presenze in un corso
#############################################
public class CourseAttendanceStatsDTO {
    private double totalLessons;
    private double averagePresencesPerLesson;
}
```
---

## API REST

### ATTENDANCES ENDPOINTS
```bash
#############################################
# Crea nuova presenza
# @func: createAttendance()
# @param: AttendanceDTO attendanceDTO
# @return: ResponseEntity<Void>
#############################################
POST    /attendances
```

---

```bash
#############################################
# Modifica presenza esistente
# @func: updateAttendance()
# @param: String attendanceId
# @param: AttendanceUpdateDTO updateDTO
# @return: ResponseEntity<Void>
#############################################
PUT     /attendances/{attendanceId}
```

---

```bash
#############################################
# Elimina una presenza
# @func: deleteAttendance()
# @param: String attendanceId
# @return: ResponseEntity<Void>
#############################################
DELETE  /attendances/{attendanceId}
```

---

```bash
#############################################
# Visualizza presenze per studente
# @func: getAttendancesByStudent()
# @param: String studentId
# @return: ResponseEntity<List<AttendanceDTO>>
#############################################
GET     /attendances/student/{studentId}
```

---

```bash
#############################################
# Visualizza presenze per corso
# @func: getAttendancesByCourse()
# @param: String courseId
# @return: ResponseEntity<List<AttendanceDTO>>
#############################################
GET     /attendances/course/{courseId}
```

---

```bash
#############################################
# Percentuale presenze studente per corso
# @func: getStudentAttendancePercentageForCourse()
# @param: String studentId, String courseId
# @return: ResponseEntity<AttendanceStatsDTO>
#############################################
GET     /attendances/student/{studentId}/course/{courseId}/attendance-percentage
```

---

```bash
#############################################
# Media presenze studenti per corso
# @func: getAverageAttendanceForCourse()
# @param: String courseId
# @return: ResponseEntity<CourseAttendanceStatsDTO>
#############################################
GET     /attendances/course/{courseId}/attendance-average
```

---

## Autenticazione e Autorizzazione

- Tutti gli endpoint richiedono autenticazione.
- I ruoli utente determinano l'accesso:
  - **Docenti**: possono registrare, modificare presenze e visualizzare la media di presenze.
  - **Studenti**: possono visualizzare le proprie presenze/statistiche in percentuale.
  - **Admin**: accesso completo.

---

## Collaborazioni con altri Microservizi

- `Course_Management`: verifica esistenza corsi, lezioni pianificate.
- `User_Roles_Management`: verifica studenti/docenti e ruoli.
- `Report_Management`: riceve le statistiche sulle percentuali delle presenze di un singolo studente ad un corso e la media delle presenze totali ad un corso.

---

### RabbitMQ - Published Events
- `attendance.created`: Quando viene registrata una nuova presenza
- `attendance.updated`: Quando una presenza viene modificata (es. da assente a presente)
- `attendance.deleted`: Quando una presenza viene eliminata
- `attendance.stats.generated`: Quando viene calcolata una statistica (percentuale o media) per il servizio Report

### RabbitMQ - Consumed Events
- `course.scheduled`: Per sincronizzare le date delle lezioni dai corsi appena creati
- `course.updated`: Per aggiornare o invalidare presenze dopo modifiche al corso
- `user.deleted`: Per eliminare automaticamente le presenze legate a uno studente rimosso
- `report.requested`: Per generare e inviare la statistica di presenze richiesta dal microservizio Report

---

## Note

- Le statistiche sono utili per il monitoraggio dell'assiduità.
- L'API segue le convenzioni REST e usa Swagger per la documentazione.
- Gli ID delle presenze sono generati automaticamente come stringhe UUID.
