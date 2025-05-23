# Attendance Management Microservice API

## Descrizione

Questo microservizio si occupa della registrazione, modifica, visualizzazione e analisi delle presenze degli studenti alle lezioni universitarie. È parte del sistema distribuito "NewUnimol" e comunica con altri microservizi come CourseService e UserService.

---

## Endpoints

### 1. Registrazione Presenza

- **Metodo:** `POST`
- **Endpoint:** `/api/attendances`
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
- **Endpoint:** `/api/attendances/{attendanceId}`
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
- **Endpoint:** `/api/attendances/{attendanceId}`
- **Descrizione:** Elimina una registrazione di presenza.

---

### 4. Visualizzazione Presenze di uno Studente

- **Metodo:** `GET`
- **Endpoint:** `/api/attendances/student/{studentId}`
- **Descrizione:** Restituisce tutte le presenze di uno studente.

---

### 5. Visualizzazione Presenze per Corso

- **Metodo:** `GET`
- **Endpoint:** `/api/attendances/course/{courseId}`
- **Descrizione:** Restituisce tutte le presenze per un corso.

---

### 6. Percentuale presenze di uno studente per un corso

- **Metodo:** `GET`
- **Endpoint:** `/api/attendances/student/{studentId}/course/{courseId}/attendance-percentage`
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

### 7. Media delle presenze di tutti gli studenti a un cors

- **Metodo:** `GET`
- **Endpoint:** `/api/attendances/course/{courseId}/attendance-average`
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
  - **Docenti**: possono registrare e modificare presenze.
  - **Studenti**: possono visualizzare le proprie presenze/statistiche.
  - **Admin**: accesso completo.

---

## Collaborazioni con altri Microservizi

- `CourseManagement`: verifica esistenza corsi, lezioni pianificate.
- `UserRolesManagement`: verifica studenti/docenti e ruoli.
- `AnalisiReportistica`: riceve le statistiche sulle percentuali delle presenze di un singolo studente ad un corso e la media delle presenze totali ad un corso.


---

## Note

- Le statistiche sono utili per il monitoraggio dell'assiduità.
- L'API segue le convenzioni REST e usa Swagger per la documentazione.
- Gli ID delle presenze sono generati automaticamente come stringhe UUID.
