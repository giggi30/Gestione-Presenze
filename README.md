# Guida Avvio e Test Microservizio

## 1. Avvio di MySQL Workbench e Configurazione Database

1. **Apri MySQL Workbench**.
2. **Connettiti** al server MySQL locale (`localhost:3306`).
3. **Crea il database** per il microservizio:
   ```sql
   CREATE DATABASE newunimol;
   ```
4. **(Opzionale)** Verifica che l'utente `Luigi` (password `Giggi123`) abbia accesso al database.

---

## 2. Avvio del Microservizio

1. **Apri il terminale** nella cartella del progetto.
2. Esegui il comando:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(oppure `mvn spring-boot:run` se hai Maven installato globalmente)*
3. Attendi che l'applicazione sia in esecuzione su `http://localhost:8080`.

---

## 3. Test del Microservizio con Postman

1. **Apri Postman**.
2. **Crea una nuova richiesta** per testare le API. Esempi:

   ### Aggiungi una presenza
   - **Metodo:** `POST`
   - **URL:** `http://localhost:8080/api/attendances`
   - **Body (JSON):**
     ```json
     {
       "studentId": "12345",
       "courseId": "CS101",
       "lessonDate": "2024-03-20",
       "status": "PRESENT"
     }
     ```

   ### Visualizza presenze di uno studente
   - **Metodo:** `GET`
   - **URL:** `http://localhost:8080/api/attendances/student/12345`

   ### Visualizza presenze di un corso
   - **Metodo:** `GET`
   - **URL:** `http://localhost:8080/api/attendances/course/CS101`

3. **Invia le richieste** e verifica le risposte.

---

## 4. Verifica dei Dati nel Database

1. Torna su **MySQL Workbench**.
2. Esegui:
   ```sql
   USE newunimol;
   SELECT * FROM presenza;
   ```
   per vedere i dati inseriti dal microservizio.

---

## 5. Test degli Eventi RabbitMQ

### Avvio di RabbitMQ

1. Accedi alla UI su [http://localhost:15672](http://localhost:15672) (user: guest, pass: guest).

---

### Pubblicazione di un evento su RabbitMQ (esempio: richiesta statistiche)

1. Vai nella UI RabbitMQ → "Queues" → cerca `report.requested.queue`.
2. Scorri in basso fino a "Publish message".
3. Inserisci nel campo "Payload" un JSON come questo:
   ```json
   {
     "requestId": "req-001",
     "studentId": "12345",
     "courseId": "67890",
     "reportType": "percentage"
   }
   ```
   Oppure per la media corso:
   ```json
   {
     "requestId": "req-002",
     "studentId": null,
     "courseId": "67890",
     "reportType": "average"
   }
   ```
4. Premi "Publish Message".

---

### Verifica della ricezione dell'evento

1. Dopo pochi secondi, vai su "Queues" → `attendance.stats.generated.queue`.
2. Clicca su "Get Message(s)" per vedere il risultato della statistica generata.
3. Puoi vedere i dettagli anche nei log del microservizio.

---

### Note
- Se non vedi la coda, aggiorna la pagina "Queues" o riavvia il microservizio.
- Se il messaggio non viene consumato, controlla i log per eventuali errori di deserializzazione o configurazione.
- Puoi testare anche gli altri eventi (creazione, aggiornamento, eliminazione presenza) pubblicando sulle rispettive code.

---

### Note

- Assicurati che MySQL sia avviato prima di lanciare il microservizio.
- Le tabelle vengono create automaticamente all'avvio grazie alla configurazione JPA.
- Puoi modificare le richieste in Postman per testare altri casi. 
