#!/bin/sh
# Local-dev Keycloak entrypoint.
# Patches the master realm to ssl_required=NONE so the Keycloak Admin API
# can be used over HTTP by AdminRealmController. In production Keycloak must
# be served over HTTPS and this patch is not needed.
set -e

PG_HOST="${KC_DB_URL_HOST:-postgres-keycloak}"
PG_PORT="${KC_DB_URL_PORT:-5432}"
PG_USER="${KC_DB_USERNAME:-report_user}"
PG_PASS="${KC_DB_PASSWORD:-report_pass}"
PG_DB="${KC_DB:-keycloak_db}"
export PGPASSWORD="$PG_PASS"

wait_for_master_realm() {
  for i in $(seq 1 60); do
    count=$(psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -t -A -c "SELECT COUNT(*) FROM realm WHERE name='master';" 2>/dev/null || echo 0)
    if [ "$count" = "1" ]; then
      return 0
    fi
    sleep 2
  done
  return 1
}

patch_master_ssl() {
  psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -c "UPDATE realm SET ssl_required='NONE' WHERE name='master';" >/dev/null 2>&1 || true
}

needs_patch() {
  val=$(psql -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DB" -t -A -c "SELECT ssl_required FROM realm WHERE name='master';" 2>/dev/null || echo "")
  [ "$val" != "NONE" ]
}

if needs_patch; then
  echo "Patching master realm for HTTP local development..."
  /opt/keycloak/bin/kc.sh start-dev --import-realm --http-enabled=true --hostname-strict=false &
  KC_PID=$!

  if wait_for_master_realm; then
    patch_master_ssl
    echo "Master realm patched; restarting Keycloak..."
  else
    echo "WARNING: master realm not found in time; continuing without patch" >&2
  fi

  kill "$KC_PID" >/dev/null 2>&1 || true
  wait "$KC_PID" >/dev/null 2>&1 || true
fi

exec /opt/keycloak/bin/kc.sh start-dev --import-realm --http-enabled=true --hostname-strict=false
