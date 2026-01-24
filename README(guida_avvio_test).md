# Guida rapida: avvio e test del microservizio

Questa guida mostra i passaggi raccomandati per avviare e testare il microservizio Attendance Management in locale.

## Prerequisiti
- Java 17
- Maven (opzionale, perché è incluso lo script `./mvnw`)
- Docker & Docker Compose
- Postman o curl per testare le API

---

## 1) Quickstart consigliato (Docker Swarm)

Il progetto è configurato per l'esecuzione su **Docker Swarm**. Segui questo ordine preciso:

### A) Configurazione Ambiente
1. **Crea il file `.env`**:
   Copia il file `.env.example` in un nuovo file chiamato `.env`.
   ```bash
   cp .env.example .env
   ```

2. **Genera le chiavi JWT**:
   L'applicazione richiede una coppia di chiavi RSA in formato Base64. Generale con lo script incluso:
   ```bash
   chmod +x generate_jwt_keys.sh
   ./generate_jwt_keys.sh
   ```
   Copia le chiavi stampate a video e incollale nel file `.env` (sostituendo i valori di `JWT_PRIVATE_KEY` e `JWT_PUBLIC_KEY`).

### B) Preparazione Docker
3. **Inizializza Swarm** (se non fatto in precedenza):
   ```bash
   docker swarm init
   ```

4. **Costruisci l'immagine**:
   ```bash
   docker build -t newunimol-app:latest .
   ```

### C) Avvio dello Stack
5. **Inizializza i Secret e le Config**:
   Questo script caricherà i valori del tuo `.env` dentro Docker Swarm:
   ```bash
   chmod +x init-swarm-secrets.sh
   ./init-swarm-secrets.sh
   ```

6. **Avvia lo Stack**:
   ```bash
   docker stack deploy -c docker-compose.yml newunimol
   ```

7. **Verifica lo stato**:
   ```bash
   docker service ls
   ```
   Attendi che il servizio `newunimol_app` abbia repliche attive (es. 3/3).
   
   > **Nota Importante:** Al primo avvio, MySQL può impiegare **30-60 secondi** per inizializzarsi completamente. Se le API restituiscono errore 500 appena dopo il deploy, attendi un minuto e riprova.

Questo avvia l'intero stack (App, MySQL, RabbitMQ) utilizzando i secret e le configurazioni definite.

---

## 2) Avvio dell'applicazione (locale)

> **Nota:** Se lo stack Docker Swarm è attivo, la porta 8080 sarà occupata. Esegui `docker stack rm newunimol` per liberarla prima di avviare l'app in locale.

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

- GET  `http://localhost:8080/api/test` — endpoint di test, risponde con stringa semplice
- POST `http://localhost:8080/api/token/generate` — genera token JWT di test
- GET  `http://localhost:8080/api/token/validate` — valida token dal header
- POST `http://localhost:8080/api/createAttendance` — crea una presenza (richiede header Authorization)
- PUT  `http://localhost:8080/api/updateAttendance/{attendanceId}` — aggiorna una presenza
- DELETE `http://localhost:8080/api/deleteAttendance/{attendanceId}` — elimina una presenza
- GET  `http://localhost:8080/api/getStudentAttendances/{studentId}` — presenze di uno studente
- GET  `http://localhost:8080/api/getAttendance/{attendanceId}` — presenza tramite ID

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

## 7) Accesso al Database (MySQL)

Il database è accessibile sia tramite terminale (dentro il container) sia tramite client esterni (es. MySQL Workbench), poiché la porta è esposta su **3307** (per evitare conflitti con eventuali MySQL locali sulla 3306).

### A) Accesso tramite Client Esterno (MySQL Workbench, DBeaver)
Configura la connessione con questi parametri:
- **Hostname**: `127.0.0.1` (o `localhost`)
- **Port**: `3307`
- **Username**: Quello definito nel `.env` (es. `your_db_username`)
- **Password**: Quella definita nel `.env` (es. `your_db_password`)
- **Default Schema**: `newunimol`

### B) Accesso tramite Terminale (Docker Exec)
Se preferisci la riga di comando senza installare client:

1. **Trova l'ID del container MySQL**:
   ```bash
   docker ps --filter "name=newunimol_mysql"
   ```
   Copia il **CONTAINER ID** (es. `a1b2c3d4...`).

2. **Accedi alla shell MySQL**:
   Sostituisci `<ID_CONTAINER>` con l'ID trovato e usa l'username definito nel tuo file `.env`.
   ```bash
   docker exec -it <ID_CONTAINER> mysql -u <TUO_DB_USERNAME> -p
   ```
   Quando richiesto, inserisci la password definita nel `.env`.

3. **Esegui query SQL**:
   Una volta dentro la shell MySQL:
   ```sql
   USE newunimol;
   SHOW TABLES;
   SELECT * FROM presenze;
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


## 10) Generare e visualizzare il report di coverage (JaCoCo)

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
## 11) Arresto e Pulizia (Reset)

Quando hai finito di testare, puoi fermare tutto e pulire l'ambiente.

1. **Fermare lo stack**:
   ```bash
   docker stack rm newunimol
   ```

2. **Pulizia completa (Opzionale)**:
   Se vuoi rimuovere anche i dati del database e i secret (per ripartire da zero o cambiare credenziali), esegui:
   ```bash
   # Rimuove i secret e le config
   docker secret rm db_password mysql_root_password rabbitmq_password jwt_private_key jwt_public_key jwt_expiration
   docker config rm db_username mysql_database rabbitmq_username rabbitmq_vhost
   
   # Rimuove il volume dei dati (ATTENZIONE: perdi i dati salvati!)
   docker volume rm newunimol_mysql_data
   ```

## 12) Guida Avanzata: Aggiornamento Credenziali Database

Se hai già avviato lo stack e vuoi cambiare le credenziali (username/password) nel file `.env`, devi seguire questa procedura perché Docker Swarm memorizza i segreti in modo permanente.

1. **Ferma e Pulisci**:
   Esegui i comandi di pulizia descritti nella **Sezione 11** (rimozione stack, secret, config e volume).

2. **Aggiorna `.env`**:
   Modifica `DB_USERNAME` e `DB_PASSWORD` nel tuo file `.env`.

3. **Rigenera i Segreti**:
   ```bash
   ./init-swarm-secrets.sh
   ```

4. **Riavvia**:
   ```bash
   docker stack deploy -c docker-compose.yml newunimol
   ```
