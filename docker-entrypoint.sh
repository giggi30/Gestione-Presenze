#!/bin/sh
set -e

# Read configs from files and export as environment variables
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

# Set static environment variables
export RABBITMQ_HOST=rabbitmq
export SPRING_RABBITMQ_HOST=$RABBITMQ_HOST

export RABBITMQ_PORT=5672
export SPRING_RABBITMQ_PORT=$RABBITMQ_PORT

# Read secrets from files and export as environment variables
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

# Wait for MySQL to be available
echo "Waiting for MySQL to be available..."
MAX_RETRIES=30
RETRY_COUNT=0
while ! nc -z mysql 3306 2>/dev/null; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "MySQL not available after $MAX_RETRIES attempts, starting anyway..."
    break
  fi
  echo "MySQL not ready yet, waiting... (attempt $RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done
echo "MySQL is available, starting application..."

# Execute the main application
exec java -jar /app/app.jar "$@"
