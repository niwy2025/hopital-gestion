#!/usr/bin/env bash
set -Eeuo pipefail

# Le conteneur PostgreSQL démarre avec l'administrateur défini par POSTGRES_USER.
# Chaque base reste logiquement isolée ; les services métier partagent pour le
# moment le rôle hospital, comme ils partageaient auparavant le compte sa.
create_role() {
  local role_name="$1"
  local role_password="$2"

  psql --quiet --tuples-only --no-align --set=ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" --dbname postgres \
    --set=role_name="$role_name" \
    --set=role_password="$role_password" >/dev/null <<'SQL'
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'role_name', :'role_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'role_name');
\gexec
SQL
}

create_database() {
  local database_name="$1"
  local owner_name="$2"

  psql --quiet --tuples-only --no-align --set=ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" --dbname postgres \
    --set=database_name="$database_name" \
    --set=owner_name="$owner_name" >/dev/null <<'SQL'
SELECT format('CREATE DATABASE %I OWNER %I', :'database_name', :'owner_name')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'database_name');
\gexec
SQL
}

enable_pgcrypto() {
  local database_name="$1"
  psql --quiet --set=ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$database_name" \
    --command 'CREATE EXTENSION IF NOT EXISTS pgcrypto;' >/dev/null
}

create_role "$HOSPITAL_DB_USER" "$HOSPITAL_DB_PASSWORD"
create_role "$KEYCLOAK_DB_USER" "$KEYCLOAK_DB_PASSWORD"
create_role "$KONG_PG_USER" "$KONG_PG_PASSWORD"

for database in \
  hospital_account \
  hospital_auth \
  hospital_organization \
  hospital_laboratory \
  hospital_pharmacy \
  hospital_patient \
  hospital_personnel; do
  create_database "$database" "$HOSPITAL_DB_USER"
  enable_pgcrypto "$database"
done

create_database "$KEYCLOAK_DB" "$KEYCLOAK_DB_USER"
create_database "$KONG_PG_DATABASE" "$KONG_PG_USER"
