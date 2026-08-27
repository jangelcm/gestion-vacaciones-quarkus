#!/bin/bash
set -e

# Crea múltiples bases y opcionalmente roles por microservicio.
# Variables esperadas:
# - POSTGRES_MULTIPLE_DATABASES=db1,db2,db3
# - MS_SOLICITUD_DB_USER / MS_SOLICITUD_DB_PASS / MS_SOLICITUD_DB_NAME
# - MS_APROBACIONES_DB_USER / MS_APROBACIONES_DB_PASS / MS_APROBACIONES_DB_NAME
# - MS_AUTH_DB_USER / MS_AUTH_DB_PASS / MS_AUTH_DB_NAME

create_database() {
  local database="$1"
  if [ -z "$database" ]; then
    return
  fi

  echo "Creating database '$database' if not exists"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
    SELECT 'CREATE DATABASE "${database}"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${database}')\gexec
EOSQL
}

create_role_and_grants() {
  local username="$1"
  local password="$2"
  local database="$3"

  if [ -z "$username" ] || [ -z "$password" ] || [ -z "$database" ]; then
    return
  fi

  echo "Ensuring role '$username' and grants on '$database'"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
    DO
    \
    \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${username}') THEN
        CREATE ROLE "${username}" LOGIN PASSWORD '${password}';
      END IF;
    END
    \
    \$\$;

    GRANT ALL PRIVILEGES ON DATABASE "${database}" TO "${username}";
EOSQL

  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$database" <<-EOSQL
    GRANT ALL ON SCHEMA public TO "${username}";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO "${username}";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO "${username}";
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
  for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
    create_database "$db"
  done
fi

create_role_and_grants "$MS_SOLICITUD_DB_USER" "$MS_SOLICITUD_DB_PASS" "$MS_SOLICITUD_DB_NAME"
create_role_and_grants "$MS_APROBACIONES_DB_USER" "$MS_APROBACIONES_DB_PASS" "$MS_APROBACIONES_DB_NAME"
create_role_and_grants "$MS_AUTH_DB_USER" "$MS_AUTH_DB_PASS" "$MS_AUTH_DB_NAME"

echo "PostgreSQL initialization finished"
