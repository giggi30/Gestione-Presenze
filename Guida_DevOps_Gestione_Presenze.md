# Guida allo Studio:  Evoluzione del Progetto Gestione-Presenze

## Panoramica Generale

Il progetto è stato evoluto seguendo le **best practices DevOps moderne**, implementando un ciclo completo di **Continuous Integration/Continuous Deployment (CI/CD)** che va dalla scrittura del codice fino al deployment in produzione su un cluster Docker Swarm. 

---

## 1. Pipeline di Build (CI - Continuous Integration)

### 1.1 Cos'è e perché è importante

La **pipeline di build** è un processo automatizzato che viene eseguito ogni volta che il codice viene pushato sul repository.  Il suo scopo è: 

- **Validare il codice** automaticamente prima che venga integrato
- **Eseguire i test** per garantire che le modifiche non introducano regressioni
- **Misurare la qualità del codice** attraverso metriche come la code coverage
- **Fornire feedback rapido** allo sviluppatore in caso di problemi

### 1.2 Tecnologie utilizzate

| Componente | Tecnologia | Scopo |
|------------|------------|-------|
| CI Server | **GitHub Actions** | Orchestrazione della pipeline |
| Build Tool | **Maven** | Compilazione e gestione dipendenze |
| Testing | **JUnit** | Esecuzione test unitari e di integrazione |
| Code Coverage | **JaCoCo** | Misurazione della copertura del codice |

### 1.3 Come funziona la pipeline

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   PUSH su   │────▶│   JOB 1:    │────▶│   JOB 2:    │────▶│   JOB 3:    │
│   GitHub    │     │ Build & Test│     │ Lint Docker │     │ Test Swarm  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                   │
                                                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   REPORT    │◀────│  COVERAGE   │◀────│  UnitTest   │
│   Finale    │     │   JaCoCo    │     │   JUnit     │
└─────────────┘     └─────────────┘     └─────────────┘
```

**Fasi della pipeline:**

1. **Trigger**: La pipeline si attiva automaticamente ad ogni push sul repository
2. **Checkout**: GitHub Actions scarica il codice sorgente
3. **Setup JDK**:  Viene configurato l'ambiente Java (JDK 17)
4. **Build e Lint Maven**: Compilazione e analisi statica (Checkstyle, Spotbugs) con `mvn clean package -DskipTests`
5. **Esecuzione Test**: Vengono eseguiti i test e la verifica di code coverage con `mvn test jacoco:check`
6. **Lint Dockerfile**: Verifica statico del Dockerfile usando **Hadolint** per best practices
7. **Test Swarm**: Simula un deployment su un cluster Swarm temporaneo (docker swarm init) per verificare che lo stack salga correttamente
6. **Analisi Coverage**: JaCoCo analizza quali righe di codice sono state eseguite durante i test
7. **Verifica Soglia**: La build **fallisce** se la copertura è inferiore al **70%**

### 1.4 Configurazione JaCoCo

JaCoCo (Java Code Coverage) è stato configurato con una **soglia minima del 70%** di copertura.  Questo significa che: 

- Almeno il 70% delle istruzioni del codice deve essere eseguito durante i test
- Se questa soglia non viene raggiunta, la build fallisce
- Questo garantisce che il codice sia adeguatamente testato prima di essere rilasciato

**Motivazione della scelta del 70%**:  È un buon compromesso tra qualità e praticità.  Valori troppo alti (es. 90%) possono essere difficili da raggiungere e mantenere, mentre valori troppo bassi non garantiscono una copertura significativa.

---

## 2. Semantic Versioning e Conventional Commits

### 2.1 Cos'è il Semantic Versioning

Il **Semantic Versioning** (SemVer) è uno standard per numerare le versioni del software nel formato: 

```
MAJOR.MINOR. PATCH
  │      │     └── Bug fix, correzioni retrocompatibili
  │      └──────── Nuove funzionalità retrocompatibili
  └─────────────── Cambiamenti che rompono la retrocompatibilità
```

**Esempi nel progetto:**
- `1.0.0` → Prima versione stabile
- `1.1.0` → Aggiunta funzionalità Docker Swarm (MINOR)
- `1.2.0` → Aggiunta script RSA e miglioramenti (MINOR)

### 2.2 Conventional Commits

I **Conventional Commits** sono una convenzione per scrivere messaggi di commit strutturati:

```
<tipo>:  <descrizione>

Tipi principali:
- feat:      Nuova funzionalità      → incrementa MINOR
- fix:      Correzione bug          → incrementa PATCH
- docs:     Solo documentazione     → nessun incremento
- refactor: Refactoring codice      → nessun incremento
- ci:       Modifiche CI/CD         → nessun incremento
- chore:    Manutenzione generica   → nessun incremento
```

**Motivazione**:  Usando i conventional commits, è possibile **automatizzare completamente il versioning**. Il sistema analizza i commit e determina automaticamente quale numero di versione incrementare.

### 2.3 Semantic Release

**Semantic Release** è lo strumento che automatizza il processo di rilascio:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Analizza i     │────▶│  Determina la   │────▶│  Crea Tag Git   │
│  Commit         │     │  nuova versione │     │  e Release      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

**Vantaggi:**
- **Eliminazione errori umani** nel versioning
- **Changelog automatico** basato sui commit
- **Release consistenti** e prevedibili

---

## 3. Pipeline di Release (CD - Continuous Deployment)

### 3.1 Architettura della Release Pipeline

La pipeline di release è separata dalla build e si occupa di:

1. **Creare la release** su GitHub basata sulla versione estratta
2. **Buildare l'artefatto** (JAR file) con Maven
3. **Allegare il JAR** alla Release di GitHub

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Trigger   │────▶│  Extract    │────▶│ Maven Build │────▶│   Create    │
│  (manual/   │     │  Version    │     │   (JAR)     │     │   Release   │
│   auto)     │     │             │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

### 3.2 Trigger della Release

La release può essere attivata in due modi:

1. **Automaticamente**:  Quando viene aggiornata la versione nel `pom.xml`
2. **Manualmente**: Attraverso `workflow_dispatch` (bottone su GitHub)

**Motivazione del trigger manuale**:  Permette di avere controllo su quando rilasciare in produzione, utile in contesti dove si vuole coordinare il deploy con altri team o eventi. 

---

## 4. Containerizzazione con Docker

### 4.1 Cos'è Docker e perché usarlo

**Docker** permette di impacchettare l'applicazione con tutte le sue dipendenze in un **container** isolato e portabile.

**Vantaggi:**
- **Consistenza**:  "Works on my machine" → "Works everywhere"
- **Isolamento**: L'app non interferisce con il sistema host
- **Portabilità**: Lo stesso container gira su qualsiasi sistema con Docker
- **Scalabilità**: Facile replicare container per gestire più carico

### 4.2 Struttura del Dockerfile

Il Dockerfile definisce come costruire l'immagine: 

```dockerfile
# Fase 1: Build (Multi-stage)
FROM eclipse-temurin:17 AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

# Fase 2: Runtime
FROM eclipse-temurin:17
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends netcat-openbsd wget && rm -rf /var/lib/apt/lists/*
RUN groupadd --system spring || true; \
  useradd --system --no-create-home --shell /bin/false --gid spring spring || true
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chown spring:spring /app/app.jar /app/docker-entrypoint.sh && \
  chmod 755 /app/app.jar /app/docker-entrypoint.sh
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
```

**Multi-stage build**:  Permette di avere un'immagine finale più pulita, contenente solo il JRE e l'applicazione, senza gli strumenti di build (Maven). La sicurezza è migliorata usando un utente non-root e installando solo i pacchetti necessari.

### 4.3 Docker Compose per sviluppo locale

**Docker Compose** orchestra più container insieme:

```yaml
services:
  mysql:
    image: mysql:8.0
    configs:
      - source: mysql_database
        target: /tmp/mysql_database
      - source: db_username
        target: /tmp/db_username
    secrets:
      - mysql_root_password
      - db_password
    ports:
      - "3307:3306"

  rabbitmq:
    image: rabbitmq:3-management
    configs:
      - source: rabbitmq_username
        target: /tmp/rabbitmq_username
      - source: rabbitmq_vhost
        target: /tmp/rabbitmq_vhost
    secrets:
      - source: rabbitmq_password
        target: rabbitmq_password
  
  app:
    image: newunimol-app:latest
    depends_on:
      - mysql
      - rabbitmq
    ports:
      - "8080:8080"
    secrets:
      - db_password
      - rabbitmq_password
      - jwt_private_key
      - jwt_public_key
      - jwt_expiration
```

**Porta MySQL 3307**: Scelta per evitare conflitti con eventuali istanze MySQL già in esecuzione sulla porta standard 3306.

---

## 5. Docker Swarm - Orchestrazione in Produzione

### 5.1 Cos'è Docker Swarm

**Docker Swarm** è l'orchestratore nativo di Docker per gestire cluster di container in produzione.  Trasforma un gruppo di macchine Docker in un **singolo host virtuale**.

```
                    ┌─────────────────────────────────────┐
                    │         DOCKER SWARM CLUSTER        │
                    │                                     │
┌─────────┐        │  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  Client │───────▶│  │ Manager │  │ Worker  │  │ Worker  │
└─────────┘        │  │  Node   │  │  Node   │  │  Node   │
                    │  └─────────┘  └─────────┘  └─────────┘
                    │       │            │            │
                    │       ▼            ▼            ▼
                    │  ┌─────────┐  ┌─────────┐  ┌─────────┐
                    │  │   App   │  │   App   │  │  MySQL  │
                    │  │ Replica │  │ Replica │  │         │
                    │  └─────────┘  └─────────┘  └─────────┘
                    └─────────────────────────────────────┘
```

### 5.2 Vantaggi di Docker Swarm

| Caratteristica | Descrizione |
|----------------|-------------|
| **Alta Disponibilità** | Se un nodo cade, i container vengono rischedulati su altri nodi |
| **Load Balancing** | Il traffico viene distribuito automaticamente tra le repliche |
| **Scaling** | Aumentare/diminuire le repliche con un comando |
| **Rolling Updates** | Aggiornamenti senza downtime |
| **Service Discovery** | I servizi si trovano automaticamente per nome |

### 5.3 Differenze tra Docker Compose e Docker Swarm

| Aspetto | Docker Compose | Docker Swarm |
|---------|----------------|--------------|
| **Ambiente** | Sviluppo locale | Produzione |
| **Nodi** | Singola macchina | Cluster multi-nodo |
| **Scaling** | Manuale | Automatico con repliche |
| **Health Checks** | Opzionali | Fondamentali |
| **Secrets** | File `.env` | Docker Secrets (criptati) |
| **Rete** | Bridge locale | Overlay network distribuita |

### 5.4 Configurazione Swarm Stack (docker-stack.yml)

Il file **`docker-stack.yml`** definisce lo stack per Docker Swarm: 

```yaml
services:
  mysql:
    image:  mysql:8.0
    configs:
      - source: mysql_database
        target: /tmp/mysql_database
      - source: db_username
        target: /tmp/db_username
    secrets:
      - mysql_root_password
      - db_password
    entrypoint: /bin/bash
    command: 
      - -c
      - |
        export MYSQL_DATABASE=$$(cat /tmp/mysql_database)
        export MYSQL_USER=$$(cat /tmp/db_username)
        export MYSQL_ROOT_PASSWORD=$$(cat /run/secrets/mysql_root_password)
        export MYSQL_PASSWORD=$$(cat /run/secrets/db_password)
        exec docker-entrypoint. sh mysqld
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - newunimol-net
    deploy:
      replicas: 1
      restart_policy:
        condition: on-failure
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout:  5s
      retries: 5

  rabbitmq:
    image:  rabbitmq:3-management
    configs:
      - source: rabbitmq_username
        target: /tmp/rabbitmq_username
      - source: rabbitmq_vhost
        target:  /tmp/rabbitmq_vhost
    secrets:
      - source: rabbitmq_password
        target:  rabbitmq_password
    entrypoint:  /bin/bash
    command:
      - -c
      - |
        export RABBITMQ_DEFAULT_USER=$$(cat /tmp/rabbitmq_username)
        export RABBITMQ_DEFAULT_PASS=$$(cat /run/secrets/rabbitmq_password)
        export RABBITMQ_DEFAULT_VHOST=$$(cat /tmp/rabbitmq_vhost)
        exec docker-entrypoint.sh rabbitmq-server
    ports:
      - "5672:5672"
      - "15672:15672"
    networks: 
      - newunimol-net
    deploy:
      replicas: 1
      restart_policy:
        condition:  on-failure
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval:  10s
      timeout: 5s
      retries:  5

  app:
    image:  newunimol-app:latest
    depends_on:
      - mysql
      - rabbitmq
    ports:
      - "8080:8080"
    configs:
      - source: db_username
        target: /run/configs/db_username
      - source: mysql_database
        target: /run/configs/mysql_database
      - source: rabbitmq_username
        target: /run/configs/rabbitmq_username
      - source: rabbitmq_vhost
        target: /run/configs/rabbitmq_vhost
    secrets: 
      - db_password
      - rabbitmq_password
      - jwt_private_key
      - jwt_public_key
      - jwt_expiration
    networks:
      - newunimol-net
    deploy: 
      replicas: 3
      update_config:
        parallelism: 2
        delay: 10s
      restart_policy:
        condition: on-failure
        delay: 10s
        max_attempts: 5
    healthcheck: 
      test: ["CMD-SHELL", "wget -q --spider http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout:  10s
      start_period: 60s
      retries: 3
```

#### Spiegazione dettagliata della configurazione

**Servizi definiti:**

| Servizio | Immagine | Repliche | Scopo |
|----------|----------|----------|-------|
| **mysql** | `mysql:8.0` | 1 | Database relazionale per persistenza dati |
| **rabbitmq** | `rabbitmq:3-management` | 1 | Message broker per comunicazione asincrona |
| **app** | `newunimol-app:latest` | 3 | Applicazione Spring Boot |

**Docker Configs vs Docker Secrets:**

| Tipo | Uso | Esempio | Montaggio |
|------|-----|---------|-----------|
| **Configs** | Dati non sensibili | username, nome database, vhost | `/tmp/` o `/run/configs/` |
| **Secrets** | Dati sensibili | password, chiavi JWT | `/run/secrets/` |

**Motivazione**:  I configs sono più semplici da gestire e aggiornare, mentre i secrets sono criptati e protetti.  Separandoli, si ha maggiore flessibilità e sicurezza.

**Deploy Configuration per l'App:**

```yaml
deploy:
  replicas: 3                    # Tre istanze dell'applicazione
  update_config:
    parallelism: 2               # Aggiorna 2 repliche alla volta
    delay:  10s                   # Attendi 10s tra i batch
  restart_policy: 
    condition: on-failure        # Riavvia solo se fallisce
    delay: 10s                   # Attendi 10s prima di riavviare
    max_attempts:  5              # Massimo 5 tentativi di riavvio
```

**Motivazione delle 3 repliche**:  Garantisce alta disponibilità.  Se una replica va in crash, le altre due continuano a servire le richieste.  Il load balancer di Swarm distribuisce automaticamente il traffico. 

**Parallelism 2**: Durante un rolling update, aggiorna 2 repliche contemporaneamente, lasciando sempre almeno 1 replica attiva per servire il traffico.

### 5.5 Health Checks

Gli **Health Checks** sono fondamentali in Swarm per determinare se un container è "sano":

| Servizio | Comando Health Check | Intervallo | Scopo |
|----------|---------------------|------------|-------|
| **MySQL** | `mysqladmin ping -h localhost` | 10s | Verifica che MySQL risponda |
| **RabbitMQ** | `rabbitmq-diagnostics check_running` | 10s | Verifica che RabbitMQ sia in esecuzione |
| **App** | `wget -q --spider http://localhost:8080/actuator/health` | 30s | Verifica endpoint Spring Boot Actuator |

**start_period:  60s** per l'app:  Dà all'applicazione Spring Boot 60 secondi per avviarsi prima di iniziare i controlli (Spring Boot richiede più tempo per inizializzarsi).

### 5.6 Docker Secrets - Gestione Sicura delle Credenziali

I **Docker Secrets** permettono di gestire informazioni sensibili in modo sicuro. 

**Come funzionano:**
- I secrets sono **criptati** a riposo nel cluster
- Vengono montati come file in `/run/secrets/<nome>` nel container
- Solo i servizi autorizzati possono accedervi
- Non sono mai esposti nei log o nelle ispezioni

**Motivazione rispetto a variabili d'ambiente**:  Le env vars possono essere esposte accidentalmente nei log o tramite `docker inspect`. I secrets sono più sicuri per dati sensibili come password e chiavi private.

### 5.7 Script di Gestione

#### 5.7.1 `init-swarm-secrets.sh` - Inizializzazione Secrets e Configs

Questo script automatizza la creazione di tutti i secrets e configs necessari per il deployment:

```bash
#!/bin/bash

# Carica variabili d'ambiente dal file . env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Funzione per creare un secret se non esiste
create_secret() {
  local secret_name=$1
  local secret_value=$2

  if docker secret inspect "$secret_name" > /dev/null 2>&1; then
    echo "Secret '$secret_name' already exists."
  else
    echo "$secret_value" | docker secret create "$secret_name" -
    echo "Secret '$secret_name' created."
  fi
}

# Funzione per creare un config se non esiste
create_config() {
  local config_name=$1
  local config_value=$2

  if docker config inspect "$config_name" > /dev/null 2>&1; then
    echo "Config '$config_name' already exists."
  else
    echo "$config_value" | docker config create "$config_name" -
    echo "Config '$config_name' created."
  fi
}

# Creazione secrets (dati sensibili)
create_secret "mysql_root_password" "$MYSQL_ROOT_PASSWORD"
create_secret "db_password" "$DB_PASSWORD"
create_secret "rabbitmq_password" "$RABBITMQ_PASSWORD"
create_secret "jwt_private_key" "$JWT_PRIVATE_KEY"
create_secret "jwt_public_key" "$JWT_PUBLIC_KEY"
create_secret "jwt_expiration" "$JWT_EXPIRATION"

# Creazione configs (dati non sensibili)
create_config "db_username" "$DB_USERNAME"
create_config "mysql_database" "$MYSQL_DATABASE"
create_config "rabbitmq_username" "$RABBITMQ_USERNAME"
create_config "rabbitmq_vhost" "$RABBITMQ_VHOST"

echo "All secrets and configs processed."
```

**Come funziona:**
1. Carica le variabili dal file `.env` locale
2. Per ogni secret/config, verifica se esiste già (idempotenza)
3. Se non esiste, lo crea usando il valore dalla variabile d'ambiente
4. Questo approccio permette di rieseguire lo script senza errori

**Motivazione**:  Automatizza un processo che altrimenti richiederebbe molti comandi manuali, riducendo errori e tempo di setup.

#### 5.7.2 `docker-entrypoint.sh` - Entry Point dell'Applicazione

Questo script è il **punto di ingresso** del container dell'applicazione Spring Boot:

```bash
#!/bin/bash
set -e    # Esce immediatamente se un comando fallisce
set -x    # Stampa ogni comando eseguito (debug mode)

# ===== LETTURA CONFIGS =====
if [ -f /run/configs/db_username ]; then
  export DB_USERNAME=$(cat /run/configs/db_username)
  export SPRING_DATASOURCE_USERNAME=$DB_USERNAME
fi

if [ -f /run/configs/mysql_database ]; then
  DB_NAME=$(cat /run/configs/mysql_database)
  export DB_URL="jdbc:mysql://mysql:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  export SPRING_DATASOURCE_URL=$DB_URL
fi

if [ -f /run/configs/rabbitmq_username ]; then
  export RABBITMQ_USERNAME=$(cat /run/configs/rabbitmq_username)
  export SPRING_RABBITMQ_USERNAME=$RABBITMQ_USERNAME
fi

if [ -f /run/configs/rabbitmq_vhost ]; then
  export RABBITMQ_VHOST=$(cat /run/configs/rabbitmq_vhost)
  export SPRING_RABBITMQ_VIRTUAL_HOST=$RABBITMQ_VHOST
fi

# ===== CONFIGURAZIONE STATICA =====
export RABBITMQ_HOST=rabbitmq
export SPRING_RABBITMQ_HOST=$RABBITMQ_HOST
export RABBITMQ_PORT=5672
export SPRING_RABBITMQ_PORT=$RABBITMQ_PORT

# ===== LETTURA SECRETS =====
if [ -f /run/secrets/db_password ]; then
  export DB_PASSWORD=$(cat /run/secrets/db_password)
  export SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD
fi

if [ -f /run/secrets/rabbitmq_password ]; then
  export RABBITMQ_PASSWORD=$(cat /run/secrets/rabbitmq_password)
  export SPRING_RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD
fi

if [ -f /run/secrets/jwt_private_key ]; then
  export JWT_PRIVATE_KEY=$(cat /run/secrets/jwt_private_key)
fi

if [ -f /run/secrets/jwt_public_key ]; then
  export JWT_PUBLIC_KEY=$(cat /run/secrets/jwt_public_key)
fi

if [ -f /run/secrets/jwt_expiration ]; then
  export JWT_EXPIRATION=$(cat /run/secrets/jwt_expiration)
fi

# ===== WAIT FOR MYSQL =====
echo "Waiting for MySQL to be available..."
MAX_RETRIES=30
RETRY_COUNT=0
while !  nc -z mysql 3306 2>/dev/null; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "MySQL not available after $MAX_RETRIES attempts, starting anyway..."
    break
  fi
  echo "MySQL not ready yet, waiting...  (attempt $RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done
echo "MySQL is available, starting application..."

# ===== AVVIO APPLICAZIONE =====
exec java -jar /app/app.jar "$@"
```

**Funzionalità chiave:**

| Fase | Descrizione | Motivazione |
|------|-------------|-------------|
| **Lettura Configs** | Legge file da `/run/configs/` e li converte in env vars | Spring Boot usa variabili d'ambiente per la configurazione |
| **Lettura Secrets** | Legge file da `/run/secrets/` e li converte in env vars | Separa dati sensibili da non sensibili |
| **Wait for MySQL** | Attende fino a 30 tentativi che MySQL sia raggiungibile | Evita errori di connessione all'avvio |
| **Debug Mode** | `set -x` stampa ogni comando eseguito | Facilita il troubleshooting |

**Pattern importante - `exec`**: Il comando `exec java -jar ... ` sostituisce il processo dello script con Java. Questo significa che Java diventa il processo con PID 1 nel container, ricevendo correttamente i segnali di terminazione (SIGTERM).

#### 5.7.3 `.reset-db.sh` - Reset Completo dello Stack

Questo script permette di fare un **reset completo** dello stack, eliminando anche i dati del database:

```bash
#!/bin/bash

# Definizione nomi stack e volume
STACK_NAME="newunimol"
VOLUME_NAME="${STACK_NAME}_mysql_data"

echo "⚠️  ATTENZIONE: Questo script eliminerà l'intero stack '$STACK_NAME' e il volume '$VOLUME_NAME'."
echo "    Tutti i dati nel database andranno persi!"
read -p "Sei sicuro di voler procedere? (y/N): " confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Operazione annullata."
    exit 0
fi

echo "🔻 Rimozione dello stack..."
docker stack rm "$STACK_NAME"

echo "⏳ Attesa che i container si fermino..."
until [ -z "$(docker ps -q --filter label=com.docker.stack.namespace=$STACK_NAME)" ]; do
    echo -n "."
    sleep 2
done
echo ""

echo "🧹 Pulizia container orfani..."
docker container prune -f > /dev/null

echo "🔻 Rimozione del volume..."
docker volume rm "$VOLUME_NAME" || echo "Volume non trovato o già rimosso."

echo "✅ Reset completato.  Ora puoi rieseguire il deploy con:"
echo "   docker stack deploy -c docker-stack.yml $STACK_NAME"
```

**Caratteristiche importanti:**

| Caratteristica | Descrizione |
|----------------|-------------|
| **Conferma utente** | Chiede conferma prima di procedere (operazione distruttiva) |
| **Attesa graceful** | Aspetta che tutti i container siano effettivamente terminati |
| **Pulizia completa** | Rimuove stack, container orfani e volume dati |
| **Istruzioni post-reset** | Mostra il comando per rideploy |

**Motivazione**:  Utile durante lo sviluppo quando si vuole ripartire da zero, o quando si devono applicare modifiche allo schema del database che richiedono una reinizializzazione. 

#### 5.7.4 `show-coverage-test.sh` - Visualizzazione Report Coverage

Questo script esegue i test, genera il report JaCoCo e lo apre nel browser:

```bash
#!/usr/bin/env zsh
set -euo pipefail

# 1) Esegue la build con generazione report
./mvnw clean test jacoco:report

# 2) Directory del sito generato
SITE_DIR="target/site"
if [ !  -d "$SITE_DIR/jacoco" ]; then
  echo "Report JaCoCo non trovato in $SITE_DIR/jacoco"
  exit 1
fi

# 3) Trova una porta libera a partire da 8000
PORT=8000
while lsof -iTCP: $PORT -sTCP: LISTEN >/dev/null 2>&1; do
  PORT=$((PORT + 1))
done

# 4) Avvia un server HTTP in background
cd "$SITE_DIR"
python3 -m http.server $PORT >/dev/null 2>&1 &
PID=$! 
sleep 1

URL="http://localhost:$PORT/jacoco/index. html"
echo "JaCoCo report disponibile su: $URL"

# 5) Apre il browser di default su macOS
open "$URL" 2>/dev/null || true

echo "Server HTTP in esecuzione (PID=$PID). Per chiudere:  kill $PID"
```

**Flusso di esecuzione:**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  mvn clean  │────▶│  mvn test   │────▶│   jacoco:    │────▶│   Python    │
│             │     │             │     │   report    │     │ HTTP Server │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                   │
                                                                   ▼
                                                            ┌─────────────┐
                                                            │   Browser   │
                                                            │  con Report │
                                                            └─────────────┘
```

**Motivazione**: Permette allo sviluppatore di visualizzare rapidamente la copertura del codice in modo interattivo, navigando tra classi e metodi per identificare quali parti necessitano di più test.

### 5.8 Riepilogo Script di Gestione

| Script | Scopo | Quando usarlo |
|--------|-------|---------------|
| **`init-swarm-secrets.sh`** | Crea secrets e configs in Swarm | Prima del primo deploy |
| **`docker-entrypoint. sh`** | Entry point del container app | Automatico all'avvio container |
| **`.reset-db.sh`** | Reset completo stack + database | Durante sviluppo o per clean restart |
| **`show-coverage-test.sh`** | Visualizza report coverage | Durante sviluppo per verificare copertura |

---

## 6. Sicurezza JWT con Chiavi RSA

### 6.1 Perché RSA invece di chiave simmetrica

| Aspetto | Chiave Simmetrica (HS256) | Chiavi Asimmetriche (RS256) |
|---------|---------------------------|------------------------------|
| **Chiavi** | Una sola chiave segreta | Coppia pubblica/privata |
| **Firma** | Stessa chiave per firmare e verificare | Privata firma, pubblica verifica |
| **Sicurezza** | Se compromessa, tutto è compromesso | Puoi distribuire la pubblica |
| **Microservizi** | Tutti devono avere la chiave segreta | Solo l'auth service ha la privata |

**Motivazione**: In un'architettura a microservizi, RS256 permette di: 
- Mantenere la chiave privata solo nel servizio di autenticazione
- Distribuire la chiave pubblica agli altri servizi per verificare i token
- Maggiore sicurezza in caso di compromissione

### 6.2 Flusso di Autenticazione

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐
│  Client │────▶│ Auth Service│     │ Other       │
│         │     │ (ha privata)│     │ Services    │
└─────────┘     └─────────────┘     │(hanno pubbl)│
     │                │              └─────────────┘
     │  1. Login      │                    │
     │───────────────▶│                    │
     │                │                    │
     │  2. JWT firmato│                    │
     │◀───────────────│                    │
     │                                     │
     │  3. Richiesta con JWT               │
     │────────────────────────────────────▶│
     │                                     │
     │          4. Verifica con pubblica   │
     │◀────────────────────────────────────│
```

---

## 7. Deployment Completo - Sequenza Operazioni

```bash
# 1. Inizializza Docker Swarm (solo la prima volta)
docker swarm init

# 2. Crea il file . env con tutte le variabili necessarie
cat > .env << EOF
MYSQL_ROOT_PASSWORD=rootpassword
DB_PASSWORD=dbpassword
DB_USERNAME=appuser
MYSQL_DATABASE=gestione_presenze
RABBITMQ_PASSWORD=rabbitpassword
RABBITMQ_USERNAME=rabbit
RABBITMQ_VHOST=/
JWT_PRIVATE_KEY="$(cat private. pem)"
JWT_PUBLIC_KEY="$(cat public.pem)"
JWT_EXPIRATION=86400000
EOF

# 3. Esegui lo script per creare secrets e configs
./init-swarm-secrets.sh

# 4. Builda l'immagine dell'applicazione
docker build -t newunimol-app: latest .

# 5. Deploy dello stack
docker stack deploy -c docker-stack.yml newunimol

# 6. Verifica lo stato
docker stack services newunimol
docker service logs -f newunimol_app
```

---

## 8. Riepilogo del Flusso Completo

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CICLO DI VITA COMPLETO                           │
└─────────────────────────────────────────────────────────────────────────┘

1. SVILUPPO
   Developer ──▶ Scrive codice ──▶ Commit (conventional) ──▶ Push

2. CONTINUOUS INTEGRATION
   GitHub Actions ──▶ Build ──▶ Test ──▶ Coverage Check (≥70%)
                                              │
                                    ❌ Fail   │   ✅ Pass
                                       │      │      │
                                       ▼      │      ▼
                                   Notifica   │   Merge OK
                                              │
3. CONTINUOUS DEPLOYMENT
   Merge to main ──▶ Semantic Release ──▶ Tag + Changelog
                            │
                            ▼
                     Docker Build ──▶ Push to Docker Hub

4. DEPLOYMENT IN PRODUZIONE
   Docker Swarm ──▶ Pull Image ──▶ Rolling Update ──▶ Health Check
                            │
                            ▼
                     Applicazione Live con: 
                     - 3 repliche
                     - Load balancing
                     - Auto-restart on failure
                     - Secrets sicuri
```

---

## 9. Domande Frequenti per l'Esame

**D:  Perché usare GitHub Actions invece di Jenkins?**
> GitHub Actions è integrato nativamente con GitHub, non richiede infrastruttura separata, ed è gratuito per repository pubblici.  Jenkins richiede un server dedicato e più manutenzione.

**D:  Perché la soglia di coverage è al 70% e non al 100%?**
> Il 100% è spesso impraticabile e può portare a test "finti" solo per aumentare la metrica. Il 70% è un buon compromesso che garantisce che le parti critiche siano testate senza overhead eccessivo.

**D:  Qual è la differenza tra Docker Compose e Docker Swarm?**
> Compose è per sviluppo locale su singola macchina, Swarm è per produzione su cluster multi-nodo con alta disponibilità, scaling automatico e rolling updates.

**D: Perché usare Docker Secrets invece di variabili d'ambiente?**
> I secrets sono criptati, non appaiono nei log o nelle ispezioni, e sono accessibili solo ai servizi autorizzati. Le env vars sono visibili in chiaro. 

**D:  Cosa succede se un container va in crash in Swarm?**
> Swarm rileva il fallimento tramite health check e riavvia automaticamente il container, eventualmente su un altro nodo se quello corrente non è disponibile. 

**D:  Perché usare chiavi RSA per JWT?**
> Permettono di separare chi può creare token (chiave privata) da chi può solo verificarli (chiave pubblica), aumentando la sicurezza in architetture distribuite.

**D: Qual è la differenza tra Docker Configs e Docker Secrets?**
> I Configs sono per dati non sensibili (username, nomi database) e sono più facili da aggiornare.  I Secrets sono criptati e protetti, ideali per password e chiavi. 

**D:  Perché l'app ha 3 repliche mentre MySQL ne ha solo 1? **
> L'applicazione è stateless e può essere facilmente replicata per alta disponibilità. MySQL è stateful e la sua replica richiede configurazioni più complesse (es. MySQL Cluster o replica master-slave).

