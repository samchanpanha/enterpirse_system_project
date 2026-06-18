#!/usr/bin/env bash
# Ensure the Keycloak master realm allows HTTP access in local development.
# Keycloak 24's master realm defaults to ssl_required=EXTERNAL, which blocks
# the password grant over plain HTTP. This script patches it to NONE and
# restarts Keycloak if a master admin token cannot be obtained.
#
# In production Keycloak must be served over HTTPS and this script is not used.
set -euo pipefail

PG_CONTAINER="${KEYCLOAK_PG_CONTAINER:-report-postgres-keycloak}"
KC_CONTAINER="${KEYCLOAK_CONTAINER:-report-keycloak}"
KC_URL="${KEYCLOAK_URL:-http://localhost:8180}"
ADMIN_USER="${KEYCLOAK_ADMIN_USERNAME:-admin}"
ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:-admin}"

get_token() {
  curl -s -m 5 -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password&client_id=admin-cli&username=$ADMIN_USER&password=$ADMIN_PASS" \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null || true
}

wait_healthy() {
  for i in $(seq 1 60); do
    if [ "$(docker inspect --format='{{.State.Health.Status}}' "$KC_CONTAINER" 2>/dev/null || echo starting)" = "healthy" ]; then
      return 0
    fi
    sleep 2
  done
  return 1
}

if [ -n "$(get_token)" ]; then
  echo "Keycloak master admin is reachable over HTTP"
  exit 0
fi

echo "Patching Keycloak master realm ssl_required=NONE for local HTTP..."
docker exec "$PG_CONTAINER" psql -U report_user -d keycloak_db -c \
  "UPDATE realm SET ssl_required='NONE' WHERE name='master';" >/dev/null

echo "Restarting Keycloak..."
docker restart "$KC_CONTAINER" >/dev/null

if ! wait_healthy; then
  echo "ERROR: Keycloak did not become healthy after restart" >&2
  exit 1
fi

if [ -n "$(get_token)" ]; then
  echo "Keycloak master admin is now reachable over HTTP"
else
  echo "ERROR: still cannot obtain Keycloak master admin token" >&2
  exit 1
fi
