# Attendance Management Microservice API

## Descrizione

Questo microservizio si occupa della registrazione, modifica, visualizzazione e analisi delle presenze degli studenti alle lezioni universitarie. È parte del sistema distribuito "NewUnimol" e comunica con altri microservizi come Course_Management, User_Roles_Management e Report_Management. Si può visionare il Progetto tramite Swagger scaricando il file 'attendance-management-stub.yaml', importandolo nel sito [SwaggerEditor] (https://editor.swagger.io/), oppure copiando il raw code di 'attendance-management-stub.yaml' e incollarlo su https://editor.swagger.io/.

---

## Funzionalità Principali

- **(Docenti)** Registrazione delle presenze degli studenti alle lezioni
- **(Docenti)** Modifica dello stato di presenza (presente/assente)
- **(Docenti)** Eliminazione delle registrazioni di presenze
- **(Docenti & studenti)** Visualizzazione delle presenze: _per ID, per matricola, per corso, per giorno_
- **(Docenti & studenti)** Calcolo della percentuale di presenze di uno studente per un corso
- **(Docenti)** Calcolo della media delle presenze per corso

---

## Tech Stack

- **Linguaggio di Programmazione:** Java 17
- **Framework:** Spring Boot 3.5.8
- **Database:** MySQL
- **API Documentation:** Swagger
- **Message Broker:** RabbitMQ (per la comunicazione asincrona)
- **Testing API REST:** Postman & MySQLWorkbench

---

## Architettura Database

### Tabella: presenza

| Colonna | Tipo | Descrizione | Vincoli |
|---------|------|-------------|----------|
| attendanceId | String | Identificatore univoco della presenza | PRIMARY KEY |
| studentId | String | ID dello studente | FOREIGN KEY, NOT NULL |
| courseId | String | ID del corso | FOREIGN KEY, NOT NULL |
| lessonDate | LocalDate | Data della lezione | NOT NULL |
| orarioIngresso | LocalDate | Orario d'ingresso |
| orarioUscita | LocalDate | Orario d'uscita |
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
public record AttendanceDTO(
    String attendanceId,
    String studentId,
    String courseId,
    LocalDate lessonDate,
    String status,
    LocalTime orarioIngresso,
    LocalTime orarioUscita
) {}
```

### DTO modifica Presenze
```java
#############################################
# AttendanceUpdateDTO
# @desc: DTO per la modifica di una presenza
#############################################
public record AttendanceUpdateDTO(
    String status,
    LocalTime orarioIngresso,
    LocalTime orarioUscita
) {}
```

### DTO Statistiche Presenze Studente
```java
#############################################
# AttendanceStatsDTO
# @desc: DTO per la percentuale di presenze di uno studente in un corso
#############################################
public record AttendanceStatsDTO(
    String studentId,
    String courseId,
    double attendancePercentage
) {}
```

### DTO Statistiche Media Presenze Corso
```java
#############################################
# CourseAttendanceStatsDTO
# @desc: DTO per la media delle presenze in un corso
#############################################
public record CourseAttendanceStatsDTO(
    String courseId,
    double averageAttendance
) {}
```

---

## API REST

### ATTENDANCES ENDPOINTS
```bash
#############################################
# Crea nuova presenza
# @func: createAttendance()
# @param: AttendanceDTO attendanceDTO
# @return: AttendanceDTO
#############################################
POST    /api/createAttendance
```

---

```bash
#############################################
# Modifica presenza esistente
# @func: updateAttendance()
# @param: String attendanceId
# @param: AttendanceUpdateDTO updateDTO
# @return: AttendanceDTO
#############################################
PUT     /api/updateAttendance/{attendanceId}
```

---

```bash
#############################################
# Elimina una presenza
# @func: deleteAttendance()
# @param: String attendanceId
# @return: void
#############################################
DELETE  /api/deleteAttendance/{attendanceId}
```

---
```bash
#############################################
# Visualizza una presenza tramite ID
# @func: getAttendanceById()
# @param: String attendanceId (path)
# @return: AttendanceDTO
#############################################
GET     /api/getAttendance/{attendanceId}
```

---

```bash
#############################################
# Visualizza presenze per studente
# @func: getStudentAttendances()
# @param: String studentId
# @return: List<AttendanceDTO>
#############################################
GET     /api/getStudentAttendances/{studentId}
```

---

```bash
#############################################
# Visualizza presenze per corso
# @func: getCourseAttendances()
# @param: String courseId
# @return: List<AttendanceDTO>
#############################################
GET     /api/getCourseAttendances/{courseId}
```

---

```bash
#############################################
# Visualizza presenze per giorno
# @func: getAttendancesByDay()
# @param: String date
# @return: List<AttendanceDTO>
#############################################
GET     /api/getAttendancesByDay/{date}
```

---

```bash
#############################################
# Percentuale presenze studente per corso
# @func: getStudentCourseStatistics()
# @param: String studentId, String courseId
# @return: AttendanceStatsDTO
#############################################
GET     /api/attendances/student/{studentId}/course/{courseId}/attendance-percentage
```

---

```bash
#############################################
# Media presenze studenti per corso
# @func: getCourseStatistics()
# @param: String courseId
# @return: CourseAttendanceStatsDTO
#############################################
GET     /api/attendances/course/{courseId}/attendance-average
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

## RabbitMQ - Event Driven Communication

### Descrizione

Il microservizio Attendance Management utilizza RabbitMQ come message broker per la comunicazione asincrona con gli altri microservizi del sistema NewUnimol. Questo approccio consente di disaccoppiare i servizi, migliorare la scalabilità e garantire la resilienza del sistema tramite la gestione di eventi.

### Eventi Pubblicati (Published Events)

- **attendance.created**: Quando viene registrata una nuova presenza.
- **attendance.updated**: Quando una presenza viene modificata (es. da assente a presente).
- **attendance.deleted**: Quando una presenza viene eliminata.
- **attendance.stats.generated**: Quando viene calcolata una statistica (percentuale o media) per il servizio Report.

### Eventi Consumati (Consumed Events)

- **course.scheduled**: Per sincronizzare le date delle lezioni dai corsi appena creati.
- **course.updated**: Per aggiornare o invalidare presenze dopo modifiche al corso.
- **report.requested**: Per generare e inviare la statistica di presenze richiesta dal microservizio Report.

### Esempio di struttura evento - Statistiche Generate

```json
{
  "requestId": "req-001",
  "studentId": "123456",
  "courseId": "Microservizi",
  "totalLessons": 10,
  "presentLessons": 8,
  "attendancePercentage": 80.0,
  "averagePresencesPerLesson": null,
  "timestamp": "2024-06-01T12:00:00"
}
```

_Esempio per media corso:_
```json
{
  "requestId": "req-002",
  "studentId": null,
  "courseId": "Microservizi",
  "totalLessons": 10,
  "presentLessons": null,
  "attendancePercentage": null,
  "averagePresencesPerLesson": 7.5,
  "timestamp": "2024-06-01T12:00:00"
}
```

### Esempio di struttura evento - Richiesta Report

```json
{
  "requestId": "req-003",
  "studentId": "123456",
  "courseId": "Microservizi",
  "reportType": "percentage"
}
```

---

### Flusso asincrono delle statistiche

1. Un microservizio (es. Report Management) pubblica un evento su `report.requested` per richiedere una statistica.
2. Attendance Management consuma l'evento, calcola la statistica richiesta e pubblica il risultato su `attendance.stats.generated`.
3. Il servizio che ha richiesto la statistica (o altri interessati) consuma l'evento di risposta e aggiorna la propria interfaccia o invia notifiche.

---

## Note

- Le statistiche sono utili per il monitoraggio dell'assiduità.
- L'API segue le convenzioni REST e usa Swagger per la documentazione.
- Gli ID delle presenze sono generati automaticamente come stringhe UUID.
