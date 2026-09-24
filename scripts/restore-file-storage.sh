#!/usr/bin/env bash
set -euo pipefail

BACKUP_FILE="${1:-}"

MODE="${GT_FILE_STORAGE_RESTORE_MODE:-filesystem}"

SOURCE_ROOT="${GT_FILE_STORAGE_ROOT:-storage/uploads}"
RESTORE_ROOT="${GT_FILE_RESTORE_ROOT:-storage/restore-test}"

SOURCE_VOLUME="${GT_FILE_STORAGE_DOCKER_VOLUME:-gt_uploads}"
RESTORE_VOLUME="${GT_FILE_RESTORE_DOCKER_VOLUME:-}"

if [[ -z "${BACKUP_FILE}" ]]; then
  echo "Usage: $0 <file-storage-backup.tgz>" >&2
  exit 1
fi

if [[ ! -f "${BACKUP_FILE}" ]]; then
  echo "Backup archive not found: ${BACKUP_FILE}" >&2
  exit 1
fi

CHECKSUM_FILE="${BACKUP_FILE}.sha256"

if [[ ! -f "${CHECKSUM_FILE}" ]]; then
  echo "Checksum file not found: ${CHECKSUM_FILE}" >&2
  exit 1
fi

echo "Verifying file-storage backup checksum..."
shasum -a 256 -c "${CHECKSUM_FILE}"

LIST_FILE="$(mktemp)"
DETAIL_FILE="$(mktemp)"

cleanup() {
  rm -f "${LIST_FILE}" "${DETAIL_FILE}"
}

trap cleanup EXIT

tar -tzf "${BACKUP_FILE}" > "${LIST_FILE}"
tar -tvzf "${BACKUP_FILE}" > "${DETAIL_FILE}"

if grep -Eq '(^/|(^|/)\.\.(/|$))' "${LIST_FILE}"; then
  echo "Unsafe archive path detected; restore refused." >&2
  exit 1
fi

if awk '
  $1 ~ /^[lh]/ {
    found=1
  }
  END {
    exit(found ? 0 : 1)
  }
' "${DETAIL_FILE}"
then
  echo "Archive contains symbolic/hard links; restore refused." >&2
  exit 1
fi

canonical_path() {
  local input="$1"
  local parent
  local base
  local parent_abs

  if [[ -e "${input}" ]]; then
    if [[ -d "${input}" ]]; then
      (
        cd "${input}"
        pwd -P
      )
      return
    fi

    parent="$(dirname "${input}")"
    base="$(basename "${input}")"

    parent_abs="$(
      cd "${parent}"
      pwd -P
    )"

    printf '%s/%s\n' "${parent_abs}" "${base}"
    return
  fi

  parent="$(dirname "${input}")"
  base="$(basename "${input}")"

  mkdir -p "${parent}"

  parent_abs="$(
    cd "${parent}"
    pwd -P
  )"

  printf '%s/%s\n' "${parent_abs}" "${base}"
}

case "${MODE}" in
  filesystem)
    SOURCE_ABS="$(canonical_path "${SOURCE_ROOT}")"
    RESTORE_ABS="$(canonical_path "${RESTORE_ROOT}")"

    case "${RESTORE_ABS}" in
      "${SOURCE_ABS}"|"${SOURCE_ABS}"/*)
        echo "Refusing to restore over or inside active file storage: ${SOURCE_ABS}" >&2
        exit 1
        ;;
    esac

    if [[ -d "${RESTORE_ROOT}" ]] &&
       [[ -n "$(
         find "${RESTORE_ROOT}" \
           -mindepth 1 \
           -maxdepth 1 \
           -print \
           -quit 2>/dev/null
       )" ]]
    then
      echo "Restore destination is not empty: ${RESTORE_ROOT}" >&2
      exit 1
    fi

    mkdir -p "${RESTORE_ROOT}"

    echo "Restoring filesystem file-storage backup..."
    echo "  Destination: ${RESTORE_ROOT}"

    tar -xzf "${BACKUP_FILE}" -C "${RESTORE_ROOT}"

    RESTORED_FILES="$(
      find "${RESTORE_ROOT}" -type f 2>/dev/null |
      wc -l |
      tr -d ' '
    )"

    echo "File-storage restore completed successfully."
    echo "  Restored files: ${RESTORED_FILES}"
    ;;

  docker-volume)
    command -v docker >/dev/null 2>&1 || {
      echo "Docker is required for docker-volume restore mode." >&2
      exit 1
    }

    if [[ -z "${RESTORE_VOLUME}" ]]; then
      echo "GT_FILE_RESTORE_DOCKER_VOLUME is required for docker-volume restore mode." >&2
      exit 1
    fi

    if [[ "${RESTORE_VOLUME}" == "${SOURCE_VOLUME}" ]]; then
      echo "Refusing to restore over active Docker storage volume: ${SOURCE_VOLUME}" >&2
      exit 1
    fi

    docker volume inspect "${RESTORE_VOLUME}" >/dev/null 2>&1 || {
      echo "Restore Docker volume not found: ${RESTORE_VOLUME}" >&2
      exit 1
    }

    NOT_EMPTY="$(
      docker run --rm \
        -v "${RESTORE_VOLUME}:/restore" \
        alpine:3.20 \
        sh -c '
          if [ -n "$(find /restore -mindepth 1 -maxdepth 1 -print -quit)" ]; then
            echo 1
          else
            echo 0
          fi
        '
    )"

    if [[ "${NOT_EMPTY}" != "0" ]]; then
      echo "Restore Docker volume is not empty: ${RESTORE_VOLUME}" >&2
      exit 1
    fi

    BACKUP_DIR="$(dirname "${BACKUP_FILE}")"
    BACKUP_NAME="$(basename "${BACKUP_FILE}")"

    BACKUP_DIR_ABS="$(
      cd "${BACKUP_DIR}"
      pwd -P
    )"

    echo "Restoring into isolated Docker volume..."
    echo "  Volume: ${RESTORE_VOLUME}"

    docker run --rm \
      -v "${RESTORE_VOLUME}:/restore" \
      -v "${BACKUP_DIR_ABS}:/backup:ro" \
      alpine:3.20 \
      sh -c 'tar -xzf "/backup/$1" -C /restore' \
      sh "${BACKUP_NAME}"

    echo "Docker-volume file-storage restore completed successfully."
    ;;

  *)
    echo "Unsupported GT_FILE_STORAGE_RESTORE_MODE: ${MODE}" >&2
    exit 1
    ;;
esac
