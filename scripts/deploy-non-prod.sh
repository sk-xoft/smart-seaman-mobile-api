#!/usr/bin/env bash
set -Eeuo pipefail

APP_NAME="${APP_NAME:-smart-seaman-mobile-api}"
IMAGE_NAME="${IMAGE_NAME:-smart-seaman-mobile-api:latest}"
HOST_PORT="${HOST_PORT:-30000}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"

ENV_FILE="${ENV_FILE:-/home/ssmuser/apps/config/mobile-api/non-prod/.env}"
FIREBASE_FILE="${FIREBASE_FILE:-/home/ssmuser/apps/config/mobile-api/non-prod/smart-seaman-firebase.json}"
LOG_DIR="${LOG_DIR:-/home/ssmuser/apps-logs-service/smart-seaman-mobile-api/logs}"
FCM_CREDENTIAL_FILE="${FCM_CREDENTIAL_FILE:-/app/firebase.json}"

if [[ -x "./mvnw" ]]; then
  MVN_CMD="${MVN_CMD:-./mvnw}"
else
  MVN_CMD="${MVN_CMD:-mvn}"
fi

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

require_file() {
  local file_path="$1"
  local label="$2"

  if [[ ! -f "$file_path" ]]; then
    log "ERROR: ${label} not found: ${file_path}"
    exit 1
  fi
}

require_file "$ENV_FILE" "env file"
require_file "$FIREBASE_FILE" "Firebase credential file"
mkdir -p "$LOG_DIR"

if docker ps -a --format '{{.Names}}' | grep -Fxq "$APP_NAME"; then
  log "Stopping container: ${APP_NAME}"
  docker stop "$APP_NAME" >/dev/null 2>&1 || true

  log "Removing container: ${APP_NAME}"
  docker rm "$APP_NAME" >/dev/null
else
  log "Container not found, skip stop/remove: ${APP_NAME}"
fi

if docker image inspect "$IMAGE_NAME" >/dev/null 2>&1; then
  log "Removing image: ${IMAGE_NAME}"
  docker image rm "$IMAGE_NAME" >/dev/null
else
  log "Image not found, skip remove: ${IMAGE_NAME}"
fi

log "Packaging application"
"$MVN_CMD" clean package -DskipTests

log "Building Docker image: ${IMAGE_NAME}"
docker build -t "$IMAGE_NAME" .

log "Starting container: ${APP_NAME}"
docker run \
  --name "$APP_NAME" \
  --detach \
  --restart unless-stopped \
  --env-file "$ENV_FILE" \
  --volume "${FIREBASE_FILE}:/app/firebase.json:ro" \
  --volume "${LOG_DIR}:/apps-logs-service/smart-seaman-mobile-api/logs" \
  --env "FCM_CREDENTIAL_FILE=${FCM_CREDENTIAL_FILE}" \
  --publish "${HOST_PORT}:${CONTAINER_PORT}/tcp" \
  "$IMAGE_NAME"

log "Deployment completed"
docker ps --filter "name=${APP_NAME}" --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
