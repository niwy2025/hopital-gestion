#!/usr/bin/env bash
set -Eeuo pipefail

# Le script d'initialisation de PostgreSQL ne s'exécute que lors de la toute
# première création du volume. Ce service one-shot complète donc les volumes
# déjà existants lors de l'ajout du microservice Pharmacie.
until pg_isready --host postgres --username "$POSTGRES_ADMIN_USER" --dbname postgres >/dev/null 2>&1; do
  sleep 2
done

export PGPASSWORD="$POSTGRES_ADMIN_PASSWORD"
exists="$(psql --host postgres --username "$POSTGRES_ADMIN_USER" --dbname postgres --tuples-only --no-align --command "SELECT 1 FROM pg_database WHERE datname = 'hospital_pharmacy'")"
if [[ "$exists" != "1" ]]; then
  psql --host postgres --username "$POSTGRES_ADMIN_USER" --dbname postgres \
    --set=database_name=hospital_pharmacy --set=owner_name="$HOSPITAL_DB_USER" <<'SQL'
SELECT format('CREATE DATABASE %I OWNER %I', :'database_name', :'owner_name')
\gexec
SQL
fi

psql --host postgres --username "$POSTGRES_ADMIN_USER" --dbname hospital_pharmacy \
  --command 'CREATE EXTENSION IF NOT EXISTS pgcrypto;' >/dev/null
