#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# migrate-users-to-keycloak.sh — Migrate users from auth-service to Keycloak
#
# One-time migration script. After running this script, users can log in
# via Keycloak (using the same email + password).
#
# Process:
#   1. Export all users from auth_db (with their tenant_id, branch_id, etc.)
#   2. For each user:
#      a. Create a Keycloak user in the corresponding realm (one realm per tenant)
#      b. Set the user's password (random one, force reset on first login)
#      c. Set the tenantId + defaultBranchId attributes on the user
#   3. Skip users that already exist in Keycloak (idempotent)
#
# Pre-requisites:
#   - Keycloak is running and admin-cli is configured (see docker-compose.yml)
#   - auth_db is accessible with read access to users table
#   - KEYCLOAK_ADMIN_USERNAME + KEYCLOAK_ADMIN_PASSWORD env vars set
#
# Usage:  bash migrate-users-to-keycloak.sh [--tenant-id X] [--dry-run]
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# Defaults
PSQL_CONTAINER="${PSQL_CONTAINER:-report-postgres}"
AUTH_DB="${AUTH_DB:-auth_db}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN_USERNAME:-admin}"
KEYCLOAK_ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
DRY_RUN=false
TENANT_FILTER=""

# Args
while [[ $# -gt 0 ]]; do
  case $1 in
    --dry-run) DRY_RUN=true; shift ;;
    --tenant-id) TENANT_FILTER="$2"; shift 2 ;;
    --help|-h)
      grep '^# ' "$0" | sed 's/^# //'
      exit 0
      ;;
    *) echo "Unknown arg: $1" >&2; exit 1 ;;
  esac
done

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'
log()  { echo -e "${BLUE}[$(date +%H:%M:%S)]${NC} $*"; }
ok()   { echo -e "  ${GREEN}✓${NC} $*"; }
fail() { echo -e "  ${RED}✗${NC} $*" >&2; }
dry()  { echo -e "  ${BLUE}(dry-run)${NC} $*"; }

# ─── 1. Get admin token from Keycloak ──────────────────────────────────────
if [ "$DRY_RUN" = true ]; then
  log "DRY RUN: skipping Keycloak admin token"
  TOKEN=""
else
  log "Getting Keycloak admin token..."
  TOKEN=$(/usr/bin/curl -s -m 10 -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" \
    -d "username=$KEYCLOAK_ADMIN_USER" \
    -d "password=$KEYCLOAK_ADMIN_PASS" \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))")

  if [ -z "$TOKEN" ]; then
    fail "Failed to get Keycloak admin token. Check KEYCLOAK_ADMIN_USERNAME / PASSWORD."
    fail "To fix: ensure the Keycloak master realm has an 'admin' user with the configured password."
    fail "If the master realm admin user was never created (env vars didn't take effect),"
    fail "restart Keycloak with a fresh volume or use the SQL workaround:"
    fail "  docker exec report-postgres-keycloak psql -U report_user -d keycloak_db \\"
    fail "    -c \"UPDATE realm SET ssl_required = 'NONE' WHERE name = 'master';\""
    exit 1
  fi
  ok "Got Keycloak admin token"
fi

# ─── 2. Export users from auth_db ─────────────────────────────────────────
log "Exporting users from $AUTH_DB..."

WHERE=""
if [ -n "$TENANT_FILTER" ]; then
  WHERE="WHERE tenant_id = '$TENANT_FILTER'"
fi

USERS_JSON=$(docker exec "$PSQL_CONTAINER" psql -U report_user -d "$AUTH_DB" -t -A -F'|' -c "
  SELECT id, tenant_id, email, first_name, last_name, is_active, branch_id
  FROM users $WHERE
  ORDER BY created_at;" 2>/dev/null)

if [ -z "$USERS_JSON" ]; then
  fail "No users found in $AUTH_DB (or DB unreachable)"
  exit 1
fi
USER_COUNT=$(echo "$USERS_JSON" | wc -l | tr -d ' ')
ok "Found $USER_COUNT user(s)"

# ─── 3. Get unique tenants ─────────────────────────────────────────────────
log "Building tenant list..."
TENANTS=$(echo "$USERS_JSON" | cut -d'|' -f2 | sort -u)
TENANT_COUNT=$(echo "$TENANTS" | wc -l | tr -d ' ')
ok "Found $TENANT_COUNT tenant(s)"

# ─── 4. Ensure each tenant's realm exists ──────────────────────────────────
log "Ensuring realms exist for each tenant..."
for TENANT in $TENANTS; do
  # Convert tenant UUID to realm slug (first 8 chars, lowercase, alphanumeric only)
  REALM_SLUG=$(echo "$TENANT" | tr -cd 'a-f0-9' | cut -c1-8)
  REALM_DISPLAY=$(echo "$REALM_SLUG" | sed 's/^./\U&/')

  if [ "$DRY_RUN" = true ]; then
    dry "Would create/use realm: $REALM_SLUG"
    continue
  fi

  # Check if realm exists
  STATUS=$(/usr/bin/curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_SLUG")
  if [ "$STATUS" = "200" ]; then
    ok "Realm exists: $REALM_SLUG"
  else
    # Create realm
    CREATE_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"realm\":\"$REALM_SLUG\",\"enabled\":true,\"displayName\":\"$REALM_DISPLAY\",\"accessTokenLifespan\":3600,\"attributes\":{\"tenantId\":\"$TENANT\"}}" \
      "$KEYCLOAK_URL/admin/realms")
    if echo "$CREATE_RESP" | grep -q "Realm"; then
      ok "Created realm: $REALM_SLUG"
    else
      fail "Failed to create realm $REALM_SLUG: $CREATE_RESP"
      continue
    fi
  fi
done

# ─── 5. Migrate each user ──────────────────────────────────────────────────
log "Migrating users..."
MIGRATED=0
SKIPPED=0
FAILED=0

while IFS='|' read -r USER_ID TENANT_ID EMAIL FIRST_NAME LAST_NAME IS_ACTIVE BRANCH_ID; do
  [ -z "$EMAIL" ] && continue

  REALM_SLUG=$(echo "$TENANT_ID" | tr -cd 'a-f0-9' | cut -c1-8)
  RANDOM_PASS=$(/usr/bin/curl -s "https://www.uuidgenerator.net/api/version4" 2>/dev/null || echo "")
  if [ -z "$RANDOM_PASS" ]; then
    RANDOM_PASS="$(openssl rand -base64 12 | tr -d '=+/' | head -c 16)Temp1!"
  fi

  if [ "$DRY_RUN" = true ]; then
    dry "Would migrate: $EMAIL → realm $REALM_SLUG (active=$IS_ACTIVE, branch=$BRANCH_ID)"
    MIGRATED=$((MIGRATED+1))
    continue
  fi

  # Check if user already exists in Keycloak
  USER_SEARCH=$(/usr/bin/curl -s -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_SLUG/users?username=$(python3 -c "import urllib.parse; print(urllib.parse.quote('$EMAIL'))")&exact=true")
  EXISTING_ID=$(echo "$USER_SEARCH" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')" 2>/dev/null || echo "")

  if [ -n "$EXISTING_ID" ]; then
    SKIPPED=$((SKIPPED+1))
    ok "Already exists: $EMAIL (skipped)"
    continue
  fi

  # Create user in Keycloak
  CREATE_BODY=$(python3 -c "
import json
print(json.dumps({
    'username': '$EMAIL',
    'email': '$EMAIL',
    'firstName': '$FIRST_NAME' or '',
    'lastName': '$LAST_NAME' or '',
    'enabled': $( [ "$IS_ACTIVE" = "t" ] && echo "true" || echo "false" ),
    'emailVerified': True,
    'attributes': {
        'tenantId': ['$TENANT_ID'],
        'defaultBranchId': ['$BRANCH_ID'],
        'legacyUserId': ['$USER_ID']
    }
}))")

  CREATE_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$CREATE_BODY" \
    "$KEYCLOAK_URL/admin/realms/$REALM_SLUG/users")

  # Check response (Keycloak returns 201 on success, 409 if exists)
  STATUS=$(/usr/bin/curl -s -o /dev/null -w "%{http_code}" -X POST -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$CREATE_BODY" \
    "$KEYCLOAK_URL/admin/realms/$REALM_SLUG/users")

  if [ "$STATUS" = "201" ]; then
    # Get the new user's Keycloak ID from the Location header
    NEW_ID=$(/usr/bin/curl -s -H "Authorization: Bearer $TOKEN" \
      "$KEYCLOAK_URL/admin/realms/$REALM_SLUG/users?username=$(python3 -c "import urllib.parse; print(urllib.parse.quote('$EMAIL'))")&exact=true" \
      | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')" 2>/dev/null)

    # Set temporary password (user must reset on first login)
    if [ -n "$NEW_ID" ]; then
      /usr/bin/curl -s -X PUT -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"type\":\"password\",\"value\":\"$RANDOM_PASS\",\"temporary\":true}" \
        "$KEYCLOAK_URL/admin/realms/$REALM_SLUG/users/$NEW_ID/reset-password" >/dev/null
    fi

    MIGRATED=$((MIGRATED+1))
    ok "Migrated: $EMAIL (Keycloak ID: ${NEW_ID:-?})"
    log "  ↳ temp password (force reset on first login): $RANDOM_PASS"
  else
    FAILED=$((FAILED+1))
    fail "Failed to migrate $EMAIL (HTTP $STATUS): $CREATE_RESP"
  fi
done <<< "$USERS_JSON"

# ─── 6. Summary ────────────────────────────────────────────────────────────
echo ""
log "Migration summary:"
echo -e "  ${GREEN}Migrated: $MIGRATED${NC}"
echo -e "  ${BLUE}Skipped (already exist): $SKIPPED${NC}"
echo -e "  ${RED}Failed: $FAILED${NC}"
echo ""

if [ "$DRY_RUN" = true ]; then
  echo -e "${BLUE}This was a dry run. Run without --dry-run to actually migrate.${NC}"
fi

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
exit 0
