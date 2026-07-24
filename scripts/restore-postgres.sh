#!/usr/bin/env bash
set -euo pipefail

BACKUP_FILE="${1:-}"
SOURCE_DB="${GT_DB_NAME:-growtogether}"
DB_USER="${GT_DB_USER:-growtogether}"
RESTORE_DB="${GT_RESTORE_DB:-growtogether_restore_test}"

if [[ -z "${BACKUP_FILE}" ]]; then
  echo "Usage: $0 <backup-file.dump>" >&2
  exit 1
fi

if [[ ! -f "${BACKUP_FILE}" ]]; then
  echo "Backup file not found: ${BACKUP_FILE}" >&2
  exit 1
fi

CHECKSUM_FILE="${BACKUP_FILE}.sha256"

if [[ -f "${CHECKSUM_FILE}" ]]; then
  echo "Verifying backup checksum..."
  shasum -a 256 -c "${CHECKSUM_FILE}"
else
  echo "Warning: checksum file not found: ${CHECKSUM_FILE}" >&2
fi

if [[ "${RESTORE_DB}" == "${SOURCE_DB}" ]]; then
  echo "Refusing to restore over the active database: ${SOURCE_DB}" >&2
  exit 1
fi

echo "Preparing isolated restore database: ${RESTORE_DB}"

docker compose exec -T postgres \
  dropdb \
  --username="${DB_USER}" \
  --if-exists \
  "${RESTORE_DB}"

docker compose exec -T postgres \
  createdb \
  --username="${DB_USER}" \
  "${RESTORE_DB}"

echo "Restoring backup..."

cat "${BACKUP_FILE}" | docker compose exec -T postgres \
  pg_restore \
  --username="${DB_USER}" \
  --dbname="${RESTORE_DB}" \
  --no-owner \
  --no-privileges \
  --exit-on-error

echo "Restore completed successfully."

docker compose exec -T postgres \
  psql \
  --username="${DB_USER}" \
  --dbname="${RESTORE_DB}" \
  --command="SELECT current_database() AS restored_database;"
