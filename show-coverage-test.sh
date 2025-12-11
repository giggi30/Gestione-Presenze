#!/usr/bin/env zsh
set -euo pipefail

# Genera i test e il report JaCoCo, poi avvia un server HTTP locale
# e stampa (apre) il link su localhost per visualizzare le statistiche.

# 1) esegue la build con generazione report
./mvnw clean test jacoco:report

# 2) directory del sito generato
SITE_DIR="target/site"
if [ ! -d "$SITE_DIR/jacoco" ]; then
  echo "Report JaCoCo non trovato in $SITE_DIR/jacoco"
  exit 1
fi

# 3) trova una porta libera a partire da 8000
PORT=8000
while lsof -iTCP:$PORT -sTCP:LISTEN >/dev/null 2>&1; do
  PORT=$((PORT + 1))
done

# 4) avvia un server HTTP in background
cd "$SITE_DIR"
python3 -m http.server $PORT >/dev/null 2>&1 &
PID=$!
sleep 1

URL="http://localhost:$PORT/jacoco/index.html"
echo "JaCoCo report disponibile su: $URL"

# 5) apre il browser di default su macOS
open "$URL" 2>/dev/null || true

echo "Server HTTP in esecuzione (PID=$PID). Per chiudere: kill $PID"
