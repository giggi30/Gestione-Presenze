# Guida rapida: avvio e test del microservizio

Questa guida mostra i passaggi raccomandati per avviare e testare il microservizio Attendance Management in locale.

## Prerequisiti
- Java 17
- Maven (opzionale, perché è incluso lo script `./mvnw`)
- Docker & Docker Compose
- Postman o curl per testare le API

---

## 1) Quickstart consigliato (Docker)

1. Avvia i servizi necessari con Docker Compose (MySQL + RabbitMQ):

   Assicurati di avere un file `.env` configurato correttamente (puoi usare `.env.example` come base).

   ```bash
   docker-compose up -d
   ```

2. Verifica che i container siano "Up" e "healthy":

   ```bash
   docker ps
   docker-compose ps
   ```

Questo avvia un database MySQL e RabbitMQ utilizzando le credenziali definite nel file `.env`.

---

## 2) Avvio dell'applicazione (locale)

Nella cartella del progetto esegui:

```bash
./mvnw spring-boot:run
```

L'applicazione sarà disponibile su: http://localhost:8080

Nota alternativa: puoi costruire il JAR con `./mvnw package` e avviare con `java -jar target/newunimol-0.0.1-SNAPSHOT.jar`.

---

## 3) Generazione e validazione token JWT (per test locale)

Per semplicità il microservizio include endpoint per generare token di prova.

- Genera un token (POST):

  URL: `POST http://localhost:8080/api/token/generate`

  Body JSON di esempio:
  ```json
  {
    "userId": "12345",
    "username": "mario.rossi",
    "role": "DOCENTE"
  }
  ```

  Risposta di esempio:
  ```json
  {
    "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "12345",
    "role": "DOCENTE",
    "issuedAt": "2024-03-20T10:00:00",
    "expiresIn": 3600
  }
  ```

- Valida un token (GET):

  URL: `GET http://localhost:8080/api/token/validate`
  Header: `Authorization: Bearer <token>`

  Risposta di esempio:
  ```json
  {
    "valid": true,
    "message": "Token valido",
    "userId": "12345",
    "role": "DOCENTE"
  }
  ```

---

## 4) Endpoint principali (URL e breve descrizione)

Base path: `/api`

- GET  `/api/test` — endpoint di test, risponde con stringa semplice
- POST `/api/token/generate` — genera token JWT di test
- GET  `/api/token/validate` — valida token dal header
- POST `/api/createAttendance` — crea una presenza (richiede header Authorization)
- PUT  `/api/updateAttendance/{attendanceId}` — aggiorna una presenza
- DELETE `/api/deleteAttendance/{attendanceId}` — elimina una presenza
- GET  `/api/getStudentAttendances/{studentId}` — presenze di uno studente
- GET  `/api/getAttendance/{attendanceId}` — presenza tramite ID

Tutte le chiamate protette richiedono l'header:

```
Authorization: Bearer <token>
```

Ruoli (per i test):
- `DOCENTE` — creare/modificare/eliminare presenze, visualizzare statistiche globali
- `STUDENTE` — visualizzare solo le proprie presenze

---

## 5) Esempio Postman: creare una presenza

- URL: `POST http://localhost:8080/api/createAttendance`
- Header: `Authorization: Bearer <token>`
- Body (JSON):
  ```json
  {
    "studentId": "12345",
    "courseId": "CS101",
    "lessonDate": "2024-03-20",
    "status": "PRESENT"
  }
  ```

---

## 6) Test RabbitMQ (UI e pubblicazione messaggi)

- UI RabbitMQ: http://localhost:15672  (guest / guest)

- Per testare una richiesta di report, pubblica su `report.requested.queue` un payload come:
  ```json
  {
    "requestId": "req-001",
    "studentId": "12345",
    "courseId": "67890",
    "reportType": "percentage"
  }
  ```

- Verifica la coda `attendance.stats.generated.queue` per i messaggi risultanti.

---

## 7) Verifica del database

Apri MySQL Workbench o usa il client MySQL e verifica i dati:

```sql
USE newunimol;
SELECT * FROM presenza;
```

---

## 8) Environment variables e sicurezza

- Per lo sviluppo si usa il file `.env` (non committare nel repository). Un esempio è presente in `.env.example`.
- Non includere chiavi o password sensibili nel repository (jwt private key, credenziali DB in chiaro). Rigenera le chiavi prima di un qualsiasi deploy pubblico.

---

## 9) Troubleshooting rapido

- 404 su endpoint: verifica il path (es. `/api/test`) e che l'app sia in esecuzione su 8080.
- Errori DB: assicurati che MySQL sia up (docker ps) e che le credenziali corrispondano a `application.properties` o variabili d'ambiente.
- RabbitMQ: controlla la UI su 15672 e i log dell'app.

---

## 10) Pipeline e CI (sintesi)

- Si consiglia di aggiungere un workflow GitHub Actions che esegua `./mvnw -B package` e i test su push/pull request.

---

## 11) Generare e visualizzare il report di coverage (JaCoCo)

Per ottenere i test e visualizzare la coverage in modo user-friendly è disponibile lo script `show-coverage-test.sh` nella root del progetto. Lo script esegue i test, genera il report JaCoCo (`target/site/jacoco`) e avvia un semplice server HTTP che espone il sito generato.

- macOS / Linux (consigliato):

  Requisiti: `python3` (per il server statico) e `lsof` (per trovare una porta libera). Se usi Linux senza `open`, il link verrà stampato e puoi aprirlo manualmente con `xdg-open` o il tuo browser.

  ```bash
  chmod +x ./show-coverage-test.sh
  ./show-coverage-test.sh
  ```

  Output tipico: lo script mostrerà l'URL (es. `http://localhost:8000/jacoco/index.html`) e aprirà il browser di default (su macOS). Il server rimane in background; per chiuderlo usa `kill <PID>` oppure interrompi il processo.

- Linux senza `open` (se il browser non si apre automaticamente):

  Dopo l'esecuzione dello script apri manualmente l'URL stampato, oppure usa:
  ```bash
  xdg-open "http://localhost:8000/jacoco/index.html"
  ```



---

## 12) Guida Docker Swarm (Deploy e Test)

Questa sezione descrive come avviare l'intero stack (App + MySQL + RabbitMQ) in modalità Docker Swarm.

### 1. Inizializzazione Swarm
Se non hai già inizializzato lo swarm:
```bash
docker swarm init
```

### 2. Build dell'immagine
Poiché lo stack usa l'immagine `newunimol-app:latest`, devi costruirla localmente (o pusharla su un registry se usi più nodi):
```bash
docker build -t newunimol-app:latest .
```

### 3. Creazione Secrets e Configs
Lo stack richiede secrets e configs per le password e le variabili sensibili. Assicurati di avere il file `.env` (vedi punto 8) e lancia lo script:
```bash
chmod +x init-swarm-secrets.sh
./init-swarm-secrets.sh
```

### 4. Deploy dello Stack
Lancia lo stack con il nome `newunimol`:
```bash
docker stack deploy -c docker-compose.yml newunimol
```

### 5. Verifica e Test
- Controlla lo stato dei servizi:
  ```bash
  docker service ls
  docker service ps newunimol_app
  ```
- Attendi che tutti i servizi abbiano repliche attive (es. 3/3 per l'app).
- L'applicazione è esposta sulla porta **8080**. Puoi testarla come descritto al punto 3:
  ```bash
  curl http://localhost:8080/api/test
  ```

### 6. Rimozione dello Stack
Per fermare e rimuovere tutto:
```bash
docker stack rm newunimol
# Opzionale: lasciare lo swarm
# docker swarm leave --force
```
