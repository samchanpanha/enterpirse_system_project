#!/usr/bin/env bash
# Add Keycloak protocol mappers to the report-system-cli client.
# Keycloak 24+ stores mapper config in a separate protocol_mapper_config table.
# Idempotent: safe to re-run.
set -euo pipefail

CLI_CONTAINER="${KEYCLOAK_PG_CONTAINER:-report-postgres-keycloak}"
REALM="${KEYCLOAK_REALM:-demo-corp}"
TARGET_CLIENT="report-system-cli"

CLIENT_ID=$(docker exec "$CLI_CONTAINER" psql -U report_user -d keycloak_db -t -A -c \
  "SELECT c.id FROM client c JOIN realm r ON c.realm_id = r.id WHERE r.name = '$REALM' AND c.client_id = '$TARGET_CLIENT';")

if [ -z "$CLIENT_ID" ]; then
  echo "ERROR: client $TARGET_CLIENT not found in realm $REALM" >&2
  exit 1
fi

# Delete any existing mappers with the names we want to manage (config first due to FK)
docker exec "$CLI_CONTAINER" psql -U report_user -d keycloak_db -c \
  "DELETE FROM protocol_mapper_config WHERE protocol_mapper_id IN \
   (SELECT id FROM protocol_mapper WHERE client_id = '$CLIENT_ID' AND name IN ('tenantId','defaultBranchId','realm-roles'));" >/dev/null
docker exec "$CLI_CONTAINER" psql -U report_user -d keycloak_db -c \
  "DELETE FROM protocol_mapper WHERE client_id = '$CLIENT_ID' AND name IN ('tenantId','defaultBranchId','realm-roles');" >/dev/null

# Insert the 3 mappers, capture their IDs
MAPPER_IDS=$(docker exec "$CLI_CONTAINER" psql -U report_user -d keycloak_db -t -A -c \
  "INSERT INTO protocol_mapper (id, name, protocol, protocol_mapper_name, client_id) VALUES
   (gen_random_uuid()::text, 'tenantId', 'openid-connect', 'oidc-usermodel-attribute-mapper', '$CLIENT_ID'),
   (gen_random_uuid()::text, 'defaultBranchId', 'openid-connect', 'oidc-usermodel-attribute-mapper', '$CLIENT_ID'),
   (gen_random_uuid()::text, 'realm-roles', 'openid-connect', 'oidc-usermodel-realm-role-mapper', '$CLIENT_ID')
   RETURNING name || '|' || id;")

TID=$(echo "$MAPPER_IDS" | grep '^tenantId|' | cut -d'|' -f2)
DID=$(echo "$MAPPER_IDS" | grep '^defaultBranchId|' | cut -d'|' -f2)
RID=$(echo "$MAPPER_IDS" | grep '^realm-roles|' | cut -d'|' -f2)
echo "  + tenantId: $TID"
echo "  + defaultBranchId: $DID"
echo "  + realm-roles: $RID"

# Insert config rows (Keycloak 24+ uses protocol_mapper_config, not a 'config' column)
docker exec "$CLI_CONTAINER" psql -U report_user -d keycloak_db -c \
  "INSERT INTO protocol_mapper_config (protocol_mapper_id, name, value) VALUES
   ('$TID', 'user.attribute', 'tenantId'),
   ('$TID', 'access.token.claim', 'true'),
   ('$TID', 'userinfo.token.claim', 'true'),
   ('$TID', 'id.token.claim', 'true'),
   ('$TID', 'claim.name', 'tenantId'),
   ('$TID', 'jsonType.label', 'String'),
   ('$DID', 'user.attribute', 'defaultBranchId'),
   ('$DID', 'access.token.claim', 'true'),
   ('$DID', 'userinfo.token.claim', 'true'),
   ('$DID', 'id.token.claim', 'true'),
   ('$DID', 'claim.name', 'defaultBranchId'),
   ('$DID', 'jsonType.label', 'String'),
   ('$RID', 'access.token.claim', 'true'),
   ('$RID', 'userinfo.token.claim', 'true'),
   ('$RID', 'id.token.claim', 'true'),
   ('$RID', 'claim.name', 'roles'),
   ('$RID', 'multivalued', 'true');" >/dev/null

echo "Mappers added (17 config rows). Restart Keycloak to apply: docker restart report-keycloak"
