#!/usr/bin/env bash
set -euo pipefail

MODE="${GT_FILE_STORAGE_BACKUP_MODE:-filesystem}"
STORAGE_ROOT="${GT_FILE_STORAGE_ROOT:-storage/uploads}"
DOCKER_VOLUME="${GT_FILE_STORAGE_DOCKER_VOLUME:-gt_uploads}"

BACKUP_DIR="${BACKUP_DIR:-backups}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

BACKUP_NAME="file-storage-${TIMESTAMP}.tgz"
BACKUP_FILE="${BACKUP_DIR}/${BACKUP_NAME}"
CHECKSUM_FILE="${BACKUP_FILE}.sha256"
MANIFEST_FILE="${BACKUP_FILE}.manifest"

mkdir -p "${BACKUP_DIR}"

filesystem_file_count() {
  find "$1" -type f 2>/dev/null |
  wc -l |
  tr -d ' '
}

filesystem_total_bytes() {
  find "$1" -type f -exec sh -c '
    for f do
      wc -c < "$f"
    done
  ' sh {} + 2>/dev/null |
  awk '{sum += $1} END {print sum + 0}'
}

case "${MODE}" in
  filesystem)
    if [[ ! -d "${STORAGE_ROOT}" ]]; then
      echo "File-storage root not found: ${STORAGE_ROOT}" >&2
      exit 1
    fi

    if [[ -n "$(
      find "${STORAGE_ROOT}" -type l -print -quit 2>/dev/null
    )" ]]
    then
      echo "File-storage source contains symbolic links; backup refused." >&2
      exit 1
    fi

    echo "Creating filesystem file-storage backup..."
    echo "  Source: ${STORAGE_ROOT}"

    tar -C "${STORAGE_ROOT}" -czf "${BACKUP_FILE}" .

    FILE_COUNT="$(filesystem_file_count "${STORAGE_ROOT}")"
    TOTAL_BYTES="$(filesystem_total_bytes "${STORAGE_ROOT}")"
    ;;

  docker-volume)
    command -v docker >/dev/null 2>&1 || {
      echo "Docker is required for docker-volume backup mode." >&2
      exit 1
    }

    docker volume inspect "${DOCKER_VOLUME}" >/dev/null 2>&1 || {
      echo "Docker volume not found: ${DOCKER_VOLUME}" >&2
      exit 1
    }

    BACKUP_DIR_ABS="$(
      cd "${BACKUP_DIR}"
      pwd -P
    )"

    echo "Creating Docker-volume file-storage backup..."
    echo "  Volume: ${DOCKER_VOLUME}"

    docker run --rm \
      -v "${DOCKER_VOLUME}:/source:ro" \
      -v "${BACKUP_DIR_ABS}:/backup" \
      alpine:3.20 \
      sh -c 'tar -C /source -czf "/backup/$1" .' \
      sh "${BACKUP_NAME}"

    FILE_COUNT="$(
      docker run --rm \
        -v "${DOCKER_VOLUME}:/source:ro" \
        alpine:3.20 \
        sh -c 'find /source -type f | wc -l' |
      tr -d ' '
    )"

    TOTAL_BYTES="$(
      docker run --rm \
        -v "${DOCKER_VOLUME}:/source:ro" \
        alpine:3.20 \
        sh -c '
          find /source -type f -exec sh -c '"'"'
            for f do
              wc -c < "$f"
            done
          '"'"' sh {} + |
          awk '"'"'{sum += $1} END {print sum + 0}'"'"'
        '
    )"
    ;;

  *)
    echo "Unsupported GT_FILE_STORAGE_BACKUP_MODE: ${MODE}" >&2
    exit 1
    ;;
esac

if [[ ! -s "${BACKUP_FILE}" ]]; then
  echo "File-storage backup failed: archive is empty." >&2
  exit 1
fi

tar -tzf "${BACKUP_FILE}" >/dev/null

shasum -a 256 "${BACKUP_FILE}" > "${CHECKSUM_FILE}"

{
  echo "mode=${MODE}"
  echo "storage_root=${STORAGE_ROOT}"
  echo "docker_volume=${DOCKER_VOLUME}"
  echo "file_count=${FILE_COUNT}"
  echo "total_bytes=${TOTAL_BYTES}"
  echo "created_at=${TIMESTAMP}"
} > "${MANIFEST_FILE}"

echo "File-storage backup created successfully:"
echo "  Archive: ${BACKUP_FILE}"
echo "  Checksum: ${CHECKSUM_FILE}"
echo "  Manifest: ${MANIFEST_FILE}"
echo "  Files: ${FILE_COUNT}"
echo "  Bytes: ${TOTAL_BYTES}"
