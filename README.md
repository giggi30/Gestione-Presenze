# Attendance Management Microservice API

## Descrizione

Questo microservizio si occupa della registrazione, modifica, visualizzazione e analisi delle presenze degli studenti alle lezioni universitarie. È parte del sistema distribuito "NewUnimol" e comunica con altri microservizi come CourseService e UserService.

---

## Funzionalità Principali

- Registrazione delle presenze degli studenti alle lezioni
- Modifica dello stato di presenza (presente/assente)
- Eliminazione delle registrazioni di presenza
- Visualizzazione delle presenze per studente
- Visualizzazione delle presenze per corso
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

## Endpoints

### 1. Registrazione Presenza

- **Metodo:** `POST`
- **Endpoint:** `/attendances`
- **Descrizione:** Registra una nuova presenza.
- **Request Body:**
```json
{
  "studentId": "string",
  "courseId": "string",
  "lessonDate": "YYYY-MM-DD",
  "status": "present" | "absent"
}
```

---

### 2. Modifica Presenza

- **Metodo:** `PUT`
- **Endpoint:** `/attendances/{attendanceId}`
- **Descrizione:** Modifica lo stato di una presenza esistente.
- **Request Body:**
```json
{
  "status": "present" | "absent"
}
```

---

### 3. Eliminazione Presenza

- **Metodo:** `DELETE`
- **Endpoint:** `/attendances/{attendanceId}`
- **Descrizione:** Elimina una registrazione di presenza.

---

### 4. Visualizzazione Presenze di uno Studente

- **Metodo:** `GET`
- **Endpoint:** `/attendances/student/{studentId}`
- **Descrizione:** Restituisce tutte le presenze di uno studente.

---

### 5. Visualizzazione Presenze per Corso

- **Metodo:** `GET`
- **Endpoint:** `/attendances/course/{courseId}`
- **Descrizione:** Restituisce tutte le presenze per un corso.

---

### 6. Percentuale presenze di uno studente per un corso

- **Metodo:** `GET`
- **Endpoint:** `/attendances/student/{studentId}/course/{courseId}/attendance-percentage`
- **Descrizione:** Calcola la percentuale di presenze dello studente in un corso.
- **Response:**
```json
{
  "totalCourseLessons": "number",
  "presentLessons": "number",
  "attendancePercentage": "number"
}
```

---

### 7. Media delle presenze di tutti gli studenti a un corso

- **Metodo:** `GET`
- **Endpoint:** `/attendances/course/{courseId}/attendance-average`
- **Descrizione:** Calcola la media delle presenze degli studenti per ogni lezione del corso.
- **Response:**
```json
{
  "totalLessons": "number",
  "averagePresencesPerLesson": "number"
}
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

## Note

- Le statistiche sono utili per il monitoraggio dell'assiduità.
- L'API segue le convenzioni REST e usa Swagger per la documentazione.
- Gli ID delle presenze sono generati automaticamente come stringhe UUID.
