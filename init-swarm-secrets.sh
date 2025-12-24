#!/bin/bash

# Load environment variables from .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Function to create a secret if it doesn't exist
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

# Function to create a config if it doesn't exist
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

# Create secrets
create_secret "mysql_root_password" "$MYSQL_ROOT_PASSWORD"
create_secret "db_password" "$DB_PASSWORD"
create_secret "rabbitmq_password" "$RABBITMQ_PASSWORD"
create_secret "jwt_private_key" "$JWT_PRIVATE_KEY"
create_secret "jwt_public_key" "$JWT_PUBLIC_KEY"
create_secret "jwt_expiration" "$JWT_EXPIRATION"

# Create configs
create_config "db_username" "$DB_USERNAME"
create_config "mysql_database" "$MYSQL_DATABASE"
create_config "rabbitmq_username" "$RABBITMQ_USERNAME"
create_config "rabbitmq_vhost" "$RABBITMQ_VHOST"

echo "All secrets and configs processed."
