#!/bin/sh
set -e

# Read configs from files and export as environment variables
if [ -f /run/configs/db_username ]; then
  export SPRING_DATASOURCE_USERNAME=$(cat /run/configs/db_username)
fi

if [ -f /run/configs/mysql_database ]; then
  DB_NAME=$(cat /run/configs/mysql_database)
  export SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
fi

if [ -f /run/configs/rabbitmq_username ]; then
  export SPRING_RABBITMQ_USERNAME=$(cat /run/configs/rabbitmq_username)
fi

if [ -f /run/configs/rabbitmq_vhost ]; then
  export SPRING_RABBITMQ_VIRTUAL_HOST=$(cat /run/configs/rabbitmq_vhost)
fi

# Set static environment variables
export SPRING_RABBITMQ_HOST=rabbitmq
export SPRING_RABBITMQ_PORT=5672

# Read secrets from files and export as environment variables
if [ -f /run/secrets/db_password ]; then
  export SPRING_DATASOURCE_PASSWORD=$(cat /run/secrets/db_password)
fi

if [ -f /run/secrets/rabbitmq_password ]; then
  export SPRING_RABBITMQ_PASSWORD=$(cat /run/secrets/rabbitmq_password)
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

# Execute the main application
exec java -jar /app/app.jar "$@"
