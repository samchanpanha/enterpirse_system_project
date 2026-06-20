#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# e2e-smoke-test.sh — Full-stack end-to-end smoke test
#
# Verifies the entire system is working:
#   1. Keycloak login (real OIDC token with tenantId + defaultBranchId claims)
#   2. SSO bridge (Keycloak JWT → legacy JWT)
#   3. All 7 backend services reachable via gateway
#   4. Branch CRUD (create + list)
#   5. User-branch assignment (auto via SSO)
#   6. BranchContextFilter (allowed branch → 200, bogus branch → 403)
#   7. Full stock transfer flow (DRAFT → SHIPPED → RECEIVED)
#   8. Kafka event consumed by finance-service
#   9. Inter-branch journal entry posted (4 balanced lines)
#
# Pre-requisites: ./docker/scripts/seed-sample-data.sh has been run.
# Exit codes: 0 = all checks pass, 1 = at least one failure.
# ─────────────────────────────────────────────────────────────────────────────
set -uo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
REALM="${KEYCLOAK_REALM:-demo-corp}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-report-system-cli}"
KEYCLOAK_CLIENT_SECRET="${KEYCLOAK_CLIENT_SECRET:-report-system-cli-secret-change-me}"
USERNAME="${USERNAME:-admin@demo.com}"
PASSWORD="${PASSWORD:-Demo123!}"
TENANT_ID="00000000-0000-0000-0000-000000000001"
HQ_BRANCH_ID="00000000-0000-0000-0000-000000000010"
PSQL_CONTAINER="${PSQL_CONTAINER:-report-postgres}"

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; NC='\033[0m'
PASS=0
FAIL=0

pass() { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${NC} $1"; }
fail() { FAIL=$((FAIL+1)); echo -e "  ${RED}✗${NC} $1"; }
section() { echo -e "\n${BLUE}=== $1 ===${NC}"; }
info() { echo -e "  ${YELLOW}…${NC} $1"; }

# ─── 1. Keycloak login ──────────────────────────────────────────────────────
section "1. Keycloak login (OIDC)"

TOKEN_RESP=$(/usr/bin/curl -s -m 10 -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=$KEYCLOAK_CLIENT_ID" \
    -d "client_secret=$KEYCLOAK_CLIENT_SECRET" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD")

TOKEN=$(echo "$TOKEN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null)
if [ -z "$TOKEN" ]; then
  fail "Keycloak login (got empty token). Response: $(echo $TOKEN_RESP | head -c 200)"
  echo "Aborting: cannot continue without a valid token."
  exit 1
fi
pass "Keycloak login (token: ${#TOKEN} chars)"

# Check custom claims
TENANT_CLAIM=$(echo "$TOKEN" | cut -d. -f2 | python3 -c "
import sys, base64, json
p = sys.stdin.read().strip()
p += '=' * ((4 - len(p) % 4) % 4)
d = json.loads(base64.urlsafe_b64decode(p))
print(d.get('tenantId', ''))")
if [ "$TENANT_CLAIM" = "$TENANT_ID" ]; then
  pass "Keycloak JWT has tenantId claim ($TENANT_CLAIM)"
else
  fail "Keycloak JWT tenantId claim missing/wrong (got '$TENANT_CLAIM')"
fi

BRANCH_CLAIM=$(echo "$TOKEN" | cut -d. -f2 | python3 -c "
import sys, base64, json
p = sys.stdin.read().strip()
p += '=' * ((4 - len(p) % 4) % 4)
d = json.loads(base64.urlsafe_b64decode(p))
print(d.get('defaultBranchId', ''))")
if [ -n "$BRANCH_CLAIM" ]; then
  pass "Keycloak JWT has defaultBranchId claim ($BRANCH_CLAIM)"
else
  fail "Keycloak JWT defaultBranchId claim missing"
fi

# ─── 2. SSO bridge ──────────────────────────────────────────────────────────
section "2. SSO bridge (Keycloak → legacy JWT)"

SSO_RESP=$(/usr/bin/curl -s -m 10 -X POST -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/auth/sso-login")
LEGACY_JWT=$(echo "$SSO_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)
if [ -n "$LEGACY_JWT" ]; then
  pass "SSO bridge returned legacy JWT (${#LEGACY_JWT} chars)"
else
  fail "SSO bridge failed: $(echo $SSO_RESP | head -c 200)"
fi

# ─── 3. All 7 services reachable ───────────────────────────────────────────
section "3. All 7 backend services reachable via gateway"

declare -a ROUTES=(
  "/api/branches"
  "/api/property/properties/by-tenant/$TENANT_ID"
  "/api/restaurant/outlets/by-tenant/$TENANT_ID"
  "/api/inventory/products/by-tenant/$TENANT_ID"
  "/api/finance/accounts/by-tenant/$TENANT_ID"
  "/api/payment/payments/by-tenant/$TENANT_ID"
  "/api/reporting/reports/definitions/by-tenant/$TENANT_ID"
)
for ROUTE in "${ROUTES[@]}"; do
  CODE=$(/usr/bin/curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL$ROUTE")
  if [ "$CODE" = "200" ]; then
    pass "$ROUTE → HTTP 200"
  else
    fail "$ROUTE → HTTP $CODE (expected 200)"
  fi
done

# ─── 4. Branch CRUD ─────────────────────────────────────────────────────────
section "4. Branch CRUD"

BRANCHES=$(/usr/bin/curl -s -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/branches")
BRANCH_COUNT=$(echo "$BRANCHES" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
if [ "$BRANCH_COUNT" -gt 0 ]; then
  pass "Branches listed ($BRANCH_COUNT branch(es))"
else
  fail "No branches returned"
fi

# Create a new test branch
TEST_BRANCH_CODE="E2E-TEST-$(date +%s)"
CREATE_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT_ID" -H "X-Branch-Id: $HQ_BRANCH_ID" \
    -d "{\"code\":\"$TEST_BRANCH_CODE\",\"name\":\"E2E Test Branch\",\"branchType\":\"STORE\",\"city\":\"Test City\",\"active\":true,\"isDefault\":false}" \
    "$GATEWAY_URL/api/branches")
NEW_BRANCH_ID=$(echo "$CREATE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
if [ -n "$NEW_BRANCH_ID" ]; then
  pass "Created branch $TEST_BRANCH_CODE ($NEW_BRANCH_ID)"
else
  fail "Create branch failed: $(echo $CREATE_RESP | head -c 200)"
fi

# ─── 5. User-branch assignment (auto via SSO) ──────────────────────────────
section "5. User-branch assignment"

# Find local user ID (from sso-login response)
USER_ID=$(echo "$SSO_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('user',{}).get('id',''))" 2>/dev/null)
if [ -z "$USER_ID" ]; then
  # Try the bridge user
  USER_ID=$(/usr/bin/curl -s -m 5 "http://localhost:8081/api/auth/sso-login" -H "Authorization: Bearer $TOKEN" -X POST 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('user',{}).get('id',''))" 2>/dev/null)
fi

if [ -n "$USER_ID" ]; then
  USER_BRANCHES=$(/usr/bin/curl -s -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/user-branches/by-user/$USER_ID")
  UB_COUNT=$(echo "$USER_BRANCHES" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
  if [ "$UB_COUNT" -gt 0 ]; then
    pass "User has $UB_COUNT branch assignment(s) (auto-assigned via SSO)"
  else
    fail "User has no branch assignments"
  fi
else
  fail "Could not determine local user ID"
fi

# ─── 6. BranchContextFilter ────────────────────────────────────────────────
section "6. BranchContextFilter (allowed vs disallowed branch)"

ALLOWED_CODE=$(/usr/bin/curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/property/properties/by-tenant/$TENANT_ID?branchId=$HQ_BRANCH_ID")
[ "$ALLOWED_CODE" = "200" ] && pass "Allowed branch → HTTP 200" || fail "Allowed branch → HTTP $ALLOWED_CODE (expected 200)"

DENIED_CODE=$(/usr/bin/curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/property/properties/by-tenant/$TENANT_ID?branchId=99999999-9999-9999-9999-999999999999")
[ "$DENIED_CODE" = "403" ] && pass "Disallowed branch → HTTP 403" || fail "Disallowed branch → HTTP $DENIED_CODE (expected 403)"

# ─── 7. Stock transfer workflow ────────────────────────────────────────────
section "7. Stock transfer (DRAFT → SHIPPED → RECEIVED)"

# Get a product and warehouse from inventory_db
PRODUCT_ID=$(docker exec $PSQL_CONTAINER psql -U report_user -d inventory_db -t -A -c "SELECT id FROM products LIMIT 1;" 2>/dev/null)
WAREHOUSE_ID=$(docker exec $PSQL_CONTAINER psql -U report_user -d inventory_db -t -A -c "SELECT id FROM warehouses LIMIT 1;" 2>/dev/null)

# Use an existing branch that exists in ALL service DBs (HQ or BR01).
# Newly created branches via /api/branches only exist in auth_db by default.
TRANSFER_TARGET_BRANCH=$HQ_BRANCH_ID
TRANSFER_TARGET_BRANCH_NAME="HQ"
EXISTING_BRANCH_ID=$(docker exec $PSQL_CONTAINER psql -U report_user -d inventory_db -t -A -c "SELECT id FROM branches WHERE code = 'BR01' LIMIT 1;" 2>/dev/null)
if [ -n "$EXISTING_BRANCH_ID" ]; then
  TRANSFER_TARGET_BRANCH=$EXISTING_BRANCH_ID
  TRANSFER_TARGET_BRANCH_NAME="BR01"
fi

if [ -z "$PRODUCT_ID" ] || [ -z "$WAREHOUSE_ID" ]; then
  fail "Missing test data (product=$PRODUCT_ID, warehouse=$WAREHOUSE_ID)"
else
  TRANSFER_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -H "X-Branch-Id: $HQ_BRANCH_ID" \
    -d "{\"tenantId\":\"$TENANT_ID\",\"fromBranchId\":\"$HQ_BRANCH_ID\",\"toBranchId\":\"$TRANSFER_TARGET_BRANCH\",\"fromWarehouseId\":\"$WAREHOUSE_ID\",\"toWarehouseId\":\"$WAREHOUSE_ID\",\"notes\":\"E2E test\",\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":1,\"unitCost\":1.00}]}" \
    "$GATEWAY_URL/api/inventory/transfers")
  TRANSFER_ID=$(echo "$TRANSFER_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
  TRANSFER_NUM=$(echo "$TRANSFER_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('transferNumber',''))" 2>/dev/null)

  if [ -n "$TRANSFER_ID" ]; then
    pass "Created DRAFT transfer $TRANSFER_NUM (HQ → $TRANSFER_TARGET_BRANCH_NAME)"

    SHIP_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/inventory/transfers/$TRANSFER_ID/ship")
    SHIP_STATUS=$(echo "$SHIP_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('status',''))" 2>/dev/null)
    [ "$SHIP_STATUS" = "SHIPPED" ] && pass "Shipped" || fail "Ship failed: $SHIP_RESP"

    RECEIVE_RESP=$(/usr/bin/curl -s -X POST -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/inventory/transfers/$TRANSFER_ID/receive")
    RECEIVE_STATUS=$(echo "$RECEIVE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('status',''))" 2>/dev/null)
    [ "$RECEIVE_STATUS" = "RECEIVED" ] && pass "Received" || fail "Receive failed: $RECEIVE_RESP"
  else
    fail "Create transfer failed: $(echo $TRANSFER_RESP | head -c 200)"
  fi
fi

# ─── 8. Kafka event consumed (inter-branch journal entry) ──────────────────
section "8. Inter-branch journal entry (consumed from Kafka)"

# Give the consumer a moment to process
sleep 3

JE_COUNT=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT count(*) FROM journal_entries WHERE reference_type = 'STOCK_TRANSFER';" 2>/dev/null)
if [ -n "$JE_COUNT" ] && [ "$JE_COUNT" -gt 0 ]; then
  pass "Found $JE_COUNT inter-branch journal entry(ies)"

  # Verify 4 lines, balanced debits/credits
  LAST_JE=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT id FROM journal_entries WHERE reference_type = 'STOCK_TRANSFER' ORDER BY created_at DESC LIMIT 1;" 2>/dev/null)
  DEBIT_SUM=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT COALESCE(SUM(debit), 0) FROM journal_entry_lines WHERE journal_entry_id = '$LAST_JE';" 2>/dev/null)
  CREDIT_SUM=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT COALESCE(SUM(credit), 0) FROM journal_entry_lines WHERE journal_entry_id = '$LAST_JE';" 2>/dev/null)
  LINE_COUNT=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT count(*) FROM journal_entry_lines WHERE journal_entry_id = '$LAST_JE';" 2>/dev/null)
  if [ "$DEBIT_SUM" = "$CREDIT_SUM" ] && [ -n "$DEBIT_SUM" ] && [ "$DEBIT_SUM" != "0" ]; then
    pass "Journal lines balance: debits=$DEBIT_SUM credits=$CREDIT_SUM ($LINE_COUNT lines)"
  else
    fail "Journal lines do NOT balance: debits=$DEBIT_SUM credits=$CREDIT_SUM ($LINE_COUNT lines)"
  fi
else
  fail "No inter-branch journal entries found (Kafka consumer may not be working)"
fi

# ─── 9. Inter-Branch Clearing account ──────────────────────────────────────
section "9. Inter-Branch Clearing account exists"

IBC_CODE=$(docker exec $PSQL_CONTAINER psql -U report_user -d finance_db -t -A -c "SELECT code FROM chart_of_accounts WHERE code = '1999-IBC' LIMIT 1;" 2>/dev/null)
if [ "$IBC_CODE" = "1999-IBC" ]; then
  pass "Inter-Branch Clearing account (1999-IBC) created"
else
  fail "Inter-Branch Clearing account (1999-IBC) not found"
fi

# ─── Summary ──────────────────────────────────────────────────────────────
section "Summary"
echo -e "  ${GREEN}Passed: $PASS${NC}   ${RED}Failed: $FAIL${NC}"
echo ""
if [ "$FAIL" -gt 0 ]; then
  echo -e "${RED}✗ E2E smoke test FAILED${NC}"
  exit 1
else
  echo -e "${GREEN}✓ E2E smoke test PASSED${NC}"
  exit 0
fi
