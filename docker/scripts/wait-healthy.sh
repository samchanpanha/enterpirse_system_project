#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# wait-healthy.sh — Wait for all docker services to become healthy
#
# Polls each service's health endpoint and waits until all are UP.
# Two check modes:
#   - HTTP: GET {protocol}://localhost:{port}{path} and accept 2xx, 401, 404
#   - TCP:  open a TCP socket to localhost:{port} and accept any successful connect
#
# Use TCP mode for infra services (postgres, redis, kafka) that don't have
# an HTTP health endpoint.
# ─────────────────────────────────────────────────────────────────────────────
set -o pipefail

TIMEOUT="${TIMEOUT:-300}"        # 5 minutes total
INTERVAL="${INTERVAL:-5}"
ELAPSED=0

GREEN='\033[0;32m'; RED='\033[0;31m'; NC='\033[0m'

log() { echo -e "[$(date +%H:%M:%S)] $*"; }

# Parallel arrays (SERVICE_NAMES[i], SERVICE_PORTS[i], CHECK_TYPE[i], PATH[i])
# CHECK_TYPE: "http" or "tcp"
SERVICE_NAMES=(
  postgres postgres-keycloak kafka redis keycloak
  eureka gateway
  auth-service property-service restaurant-service
  inventory-service finance-service payment-service reporting-service
  nuxt-web
)
SERVICE_PORTS=(
  5432 5432 9092 6379 8180
  8761 8080
  8081 8082 8083
  8084 8085 8086 8087
  3000
)
CHECK_TYPES=(
  tcp tcp tcp tcp http
  http http
  http http http
  http http http http
  http
)
SERVICE_PATHS=(
  "" "" "" "" /health/ready
  /actuator/health /actuator/health
  /actuator/health /actuator/health /actuator/health
  /actuator/health /actuator/health /actuator/health /actuator/health
  /
)

check_tcp() {
  local port=$1
  if /usr/bin/curl -s -o /dev/null -m 2 "telnet://localhost:$port" 2>/dev/null; then
    return 0
  fi
  # Fallback: try bash /dev/tcp
  (exec 3<>/dev/tcp/localhost/$port) 2>/dev/null
  return $?
}

check_http() {
  local port=$1
  local path=$2
  local code=$(/usr/bin/curl -s -o /dev/null -m 2 -w "%{http_code}" "http://localhost:$port$path" 2>/dev/null)
  if [ -z "$code" ]; then
    code="000"
  fi
  # 2xx = healthy; 401 = healthy (auth required, but service is up);
  # 404 = healthy for nuxt-web (no /actuator/health endpoint)
  case "$code" in
    2*|401|404) return 0 ;;
    *) return 1 ;;
  esac
}

log "Waiting for ${#SERVICE_NAMES[@]} services to become healthy (timeout: ${TIMEOUT}s)..."

while [ $ELAPSED -lt $TIMEOUT ]; do
  UNHEALTHY=()
  for i in "${!SERVICE_NAMES[@]}"; do
    name="${SERVICE_NAMES[$i]}"
    port="${SERVICE_PORTS[$i]}"
    ctype="${CHECK_TYPES[$i]}"
    path="${SERVICE_PATHS[$i]}"
    if [ "$ctype" = "tcp" ]; then
      if ! check_tcp "$port"; then
        UNHEALTHY+=("$name")
      fi
    else
      if ! check_http "$port" "$path"; then
        UNHEALTHY+=("$name")
      fi
    fi
  done

  if [ ${#UNHEALTHY[@]} -eq 0 ]; then
    echo -e "${GREEN}✓ All services are healthy${NC}"
    exit 0
  fi

  log "Waiting on: ${UNHEALTHY[*]}"
  sleep "$INTERVAL"
  ELAPSED=$((ELAPSED + INTERVAL))
done

echo -e "${RED}✗ Timeout waiting for services. Still unhealthy:${NC} ${UNHEALTHY[*]}"
exit 1
