#!/bin/bash

# Define stack and volume names
STACK_NAME="newunimol"
VOLUME_NAME="${STACK_NAME}_mysql_data"

echo "ATTENZIONE: Questo script eliminerà l'intero stack '$STACK_NAME' e il volume '$VOLUME_NAME'."
echo "    Tutti i dati nel database andranno persi!"
read -p "Sei sicuro di voler procedere? (y/N): " confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Operazione annullata."
    exit 0
fi

echo "Rimozione dello stack..."
docker stack rm "$STACK_NAME"

echo "Attesa che i container si fermino..."
# Wait for all containers belonging to the stack to be removed
until [ -z "$(docker ps -q --filter label=com.docker.stack.namespace=$STACK_NAME)" ]; do
    echo -n "."
    sleep 2
done
echo ""

echo "Pulizia container orfani..."
docker container prune -f > /dev/null

echo "Rimozione del volume..."
docker volume rm "$VOLUME_NAME" || echo "Volume non trovato o già rimosso."

echo "Reset completato. Ora puoi rieseguire il deploy con:"
echo "   docker stack deploy -c docker-stack.yml $STACK_NAME"
