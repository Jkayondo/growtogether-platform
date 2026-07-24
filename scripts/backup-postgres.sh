#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-backups}"
DB_NAME="${GT_DB_NAME:-growtogether}"
DB_USER="${GT_DB_USER:-growtogether}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}-${TIMESTAMP}.dump"
CHECKSUM_FILE="${BACKUP_FILE}.sha256"

mkdir -p "${BACKUP_DIR}"

echo "Creating PostgreSQL backup..."
docker compose exec -T postgres \
  pg_dump \
  --username="${DB_USER}" \
  --dbname="${DB_NAME}" \
  --format=custom \
  --no-owner \
  --no-privileges \
  > "${BACKUP_FILE}"

if [[ ! -s "${BACKUP_FILE}" ]]; then
  echo "Backup failed: output file is empty." >&2
  exit 1
fi

shasum -a 256 "${BACKUP_FILE}" > "${CHECKSUM_FILE}"

echo "Backup created successfully:"
echo "  File: ${BACKUP_FILE}"
echo "  Checksum: ${CHECKSUM_FILE}"
