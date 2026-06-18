#!/usr/bin/env bash
# Set Keycloak user attributes for the demo user.
# This is a workaround for HTTPS-only admin-cli client.
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
REALM="${KEYCLOAK_REALM:-demo-corp}"
ADMIN_USER="${KEYCLOAK_ADMIN_USERNAME:-admin}"
ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
USERNAME="${1:-admin@demo.com}"
TENANT_ID="${2:-00000000-0000-0000-0000-000000000001}"
DEFAULT_BRANCH="${3:-00000000-0000-0000-0000-000000000010}"

# Get admin token via direct grant
TOKEN=$(/usr/bin/curl -sk -m 5 -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" -d "client_id=admin-cli" \
  -d "username=${ADMIN_USER}" -d "password=${ADMIN_PASS}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  # Fall back: use master realm with public client
  TOKEN=$(/usr/bin/curl -sk -m 5 -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=client_credentials" -d "client_id=admin-cli" \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null)
fi

if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not get admin token" >&2
  exit 1
fi

# Find user
USER_ID=$(/usr/bin/curl -sk -m 5 -H "Authorization: Bearer $TOKEN" \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/users?username=${USERNAME}&exact=true" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')" 2>/dev/null)

if [ -z "$USER_ID" ]; then
  echo "ERROR: User not found: $USERNAME" >&2
  exit 1
fi

echo "Updating user $USERNAME ($USER_ID)..."

# Get current user
CURRENT=$(/usr/bin/curl -sk -m 5 -H "Authorization: Bearer $TOKEN" \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/users/${USER_ID}")

# Merge attributes
python3 << PYEOF
import json
user = json.loads('''$CURRENT''')
user.setdefault('attributes', {})
user['attributes']['tenantId'] = ['$TENANT_ID']
user['attributes']['defaultBranchId'] = ['$DEFAULT_BRANCH']
print(json.dumps(user))
PYEOF
