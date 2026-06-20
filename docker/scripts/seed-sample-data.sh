#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# seed-sample-data.sh — Generate demo data across all services
#
# Pre-requisites:
#   • All 7 microservices running (./deploy local up)
#   • Default tenant + user seeded in auth_db (id 00000000-0000-0000-0000-000000000001)
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'
log()  { echo -e "${BLUE}[$(date +%H:%M:%S)]${NC} $*"; }
ok()   { echo -e "${GREEN}  ✓${NC} $*"; }
fail() { echo -e "${RED}  ✗${NC} $*" >&2; exit 1; }

# 0. Ensure Keycloak master realm allows HTTP (dev-only; production uses HTTPS)
bash "$SCRIPT_DIR/keycloak-ensure-admin.sh" >/dev/null || true

# 0b. Ensure Keycloak mappers exist for the report-system-cli client
#    (these are also in the realm JSON for fresh imports, but this fixes
#     existing stacks where the realm was imported before the mappers were added)
log "Ensuring Keycloak mappers for report-system-cli..."
bash "$SCRIPT_DIR/keycloak-add-mappers.sh" >/dev/null && ok "Keycloak mappers present" || log "  (mappers script failed; continuing — may already be present)"

API="http://localhost:8080/api"
TENANT="00000000-0000-0000-0000-000000000001"
HQ_BRANCH="00000000-0000-0000-0000-000000000010"
EMAIL="admin@demo.com"
PASSWORD="Demo123!"

# 1. Login
log "Logging in as $EMAIL..."
LOGIN_RESP=$(/usr/bin/curl -s -m 10 -X POST "$API/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null) \
  || fail "Login failed: $LOGIN_RESP"
ok "Token acquired (${#TOKEN} chars)"

AUTH=(-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -H "X-Branch-Id: $HQ_BRANCH")

# post <path> <json> -> echoes response body, returns HTTP code via $?
post() {
  /usr/bin/curl -s -m 10 -o /tmp/seed_resp -w "%{http_code}" "${AUTH[@]}" -X POST "$1" -d "$2"
}

post_ok() {
  local code=$(post "$1" "$2")
  if [[ "$code" =~ ^2 ]]; then
    ok "$3"
  else
    local body=$(/bin/cat /tmp/seed_resp 2>/dev/null | /usr/bin/head -c 200)
    fail "$3 — HTTP $code: $body"
  fi
}

# 2. Finance: chart of accounts
log "Seeding finance: chart of accounts..."
for acct in \
  '{"code":"1000","name":"Cash","type":"ASSET","active":true,"contra":false}' \
  '{"code":"1100","name":"Accounts Receivable","type":"ASSET","active":true,"contra":false}' \
  '{"code":"2000","name":"Accounts Payable","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"4000","name":"Sales Revenue","type":"REVENUE","active":true,"contra":false}' \
  '{"code":"5000","name":"Rent Expense","type":"EXPENSE","active":true,"contra":false}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$acct" "$TENANT")
  post_ok "$API/finance/accounts" "$payload" "Account: $(echo "$acct" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

# 3. Finance: 3 employees
log "Seeding finance: employees..."
for emp in \
  '{"firstName":"Sok","lastName":"Pisey","email":"sok@demo.com","position":"Manager","baseSalary":1500,"taxDependents":2}' \
  '{"firstName":"Chan","lastName":"Dara","email":"dara@demo.com","position":"Cashier","baseSalary":600,"taxDependents":1}' \
  '{"firstName":"Meas","lastName":"Sreyleak","email":"sreyleak@demo.com","position":"Chef","baseSalary":800,"taxDependents":0}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$emp" "$TENANT")
  post_ok "$API/finance/employees" "$payload" "Employee: $(echo "$emp" | python3 -c 'import sys,json; print(json.load(sys.stdin)["firstName"])')"
done

# 4. Property: 2 properties + 4 units
log "Seeding property: 2 properties..."
P1=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/property/properties" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"BKK1 Apartments\",\"type\":\"apartment\",\"address\":\"Street 271\",\"city\":\"Phnom Penh\",\"district\":\"Chamkarmon\",\"totalUnits\":3}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))")
ok "Property 1: $P1"
P2=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/property/properties" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"BKK1 Office\",\"type\":\"office\",\"address\":\"Street 51\",\"city\":\"Phnom Penh\",\"district\":\"BKK1\",\"totalUnits\":2}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))")
ok "Property 2: $P2"

log "Seeding property: 4 units..."
for u in "A-101:1:2" "A-102:2:1" "B-201:2:0" "B-202:3:0"; do
  IFS=':' read -r uname floor beds <<< "$u"
  pid=$([ "$uname" = "A-101" -o "$uname" = "A-102" ] && echo "$P1" || echo "$P2")
  rent=$([ "$uname" = "A-101" ] && echo 800 || ([ "$uname" = "A-102" ] && echo 500 || ([ "$uname" = "B-201" ] && echo 1500 || echo 1200)))
  post_ok "$API/property/units" "{\"tenantId\":\"$TENANT\",\"propertyId\":\"$pid\",\"label\":\"$uname\",\"floor\":$floor,\"bedrooms\":$beds,\"bathrooms\":1}" "Unit $uname"
done

# 5. Restaurant: outlet + categories
log "Seeding restaurant: 1 outlet..."
OUTLET=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/restaurant/outlets" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"Main Restaurant\",\"address\":\"#15 Norodom Blvd\",\"city\":\"Phnom Penh\",\"type\":\"casual_dining\",\"currency\":\"USD\",\"taxRate\":10}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))")
ok "Outlet: $OUTLET"

log "Seeding restaurant: 3 categories..."
for cat in '{"name":"Appetizers","nameKh":"គ្រឿងស្រវឹង","sortOrder":1,"active":true}' \
           '{"name":"Mains","nameKh":"ម្ហូបមាន់","sortOrder":2,"active":true}' \
           '{"name":"Drinks","nameKh":"ភេសជ្ជៈ","sortOrder":3,"active":true}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['outletId']=sys.argv[3]; print(json.dumps(d))" "$cat" "$TENANT" "$OUTLET")
  post_ok "$API/restaurant/menu/categories" "$payload" "Category: $(echo "$cat" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

# 6. Inventory: suppliers
log "Seeding inventory: 3 suppliers..."
for sup in \
  '{"name":"Cambodia Beverage Co","contactPerson":"Mr. Long","phone":"012345678","email":"long@cambev.com","paymentTerms":"Net 30","currency":"USD","active":true}' \
  '{"name":"Phnom Penh Supply","contactPerson":"Ms. Sothy","phone":"098765432","email":"sothy@pps.com","paymentTerms":"Net 15","currency":"USD","active":true}' \
  '{"name":"Mekong Foods","contactPerson":"Mr. Vuth","phone":"011222333","email":"vuth@mekong.com","paymentTerms":"Net 30","currency":"USD","active":true}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$sup" "$TENANT")
  post_ok "$API/inventory/suppliers" "$payload" "Supplier: $(echo "$sup" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

# 7. Reporting: 2 report definitions + 1 dashboard
log "Seeding reporting: 2 report definitions..."
for rpt in '{"name":"Daily Sales","code":"DAILY_SALES","type":"TABULAR","config":"{}","system":false}' \
           '{"name":"Monthly Revenue","code":"MONTHLY_REV","type":"SUMMARY","config":"{}","system":false}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$rpt" "$TENANT")
  post_ok "$API/reporting/reports/definitions" "$payload" "Report: $(echo "$rpt" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

log "Seeding reporting: 1 dashboard..."
post_ok "$API/reporting/dashboards" "{\"tenantId\":\"$TENANT\",\"name\":\"Main Dashboard\",\"layout\":\"{}\",\"isDefault\":true}" "Dashboard"

# 8. Extract user ID and assign to HQ branch (for branch-context filtering)
log "Assigning admin user to HQ branch..."
USER_ID=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['id'])" 2>/dev/null) || fail "Could not extract user ID from login response"
post_ok "$API/user-branches" "{\"userId\":\"$USER_ID\",\"branchId\":\"$HQ_BRANCH\",\"role\":\"ADMIN\",\"isDefault\":true}" "Admin → HQ branch assignment"

# 9. Inventory: 3 products
log "Seeding inventory: 3 products..."
for prod in \
  '{"name":"Jasmine Rice 50kg","sku":"RICE-J50","unitPrice":32.50}' \
  '{"name":"Cooking Oil 5L","sku":"OIL-C5","unitPrice":8.75}' \
  '{"name":"Fish Sauce 1L","sku":"FISH-1L","unitPrice":3.25}'
do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$prod" "$TENANT")
  post_ok "$API/inventory/products" "$payload" "Product: $(echo "$prod" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

# 10. Inventory: 1 warehouse (insert directly — no API endpoint exists)
log "Seeding inventory: 1 warehouse..."
WID="00000000-0000-0000-0000-000000000100"
docker exec report-postgres psql -U report_user -d inventory_db -c \
  "INSERT INTO warehouses (id, tenant_id, branch_id, name, type, location, is_active, created_at) \
   VALUES ('$WID', '$TENANT', '$HQ_BRANCH', 'Main Warehouse', 'central', 'Phnom Penh', true, NOW()) \
   ON CONFLICT (id) DO NOTHING;" 2>/dev/null && ok "Warehouse: Main Warehouse ($WID)" \
  || fail "Warehouse insert failed"

# Ensure a second branch exists in inventory_db for stock transfer target
BR01_ID="00000000-0000-0000-0000-000000000011"
docker exec report-postgres psql -U report_user -d inventory_db -c \
  "INSERT INTO branches (id, tenant_id, code, name, branch_type, city, is_active, created_at) \
   VALUES ('$BR01_ID', '$TENANT', 'BR01', 'Branch 01', 'STORE', 'Phnom Penh', true, NOW()) \
   ON CONFLICT (id) DO NOTHING;" 2>/dev/null && ok "Branch BR01 in inventory_db" \
  || log "Branch BR01 already exists or insert skipped"

# 11. Finance: Inter-Branch Clearing account (1999-IBC)
log "Seeding finance: Inter-Branch Clearing account..."
post_ok "$API/finance/accounts" "{\"tenantId\":\"$TENANT\",\"code\":\"1999-IBC\",\"name\":\"Inter-Branch Clearing\",\"type\":\"LIABILITY\",\"active\":true,\"contra\":false}" "Account: Inter-Branch Clearing (1999-IBC)"

# ─── Summary ────────────────────────────────────────────────────────────────
echo ""
log "Summary:"
get_count() {
  /usr/bin/curl -s -m 5 "${AUTH[@]}" "$API/$1" 2>/dev/null \
    | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "?"
}
printf "  • %-14s %s\n" "Accounts"     "$(get_count finance/accounts/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Employees"    "$(get_count finance/employees/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Properties"   "$(get_count property/properties/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Outlets"      "$(get_count restaurant/outlets/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Categories"   "$(get_count restaurant/categories/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Suppliers"    "$(get_count inventory/suppliers/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Products"     "$(get_count inventory/products/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Reports"      "$(get_count reporting/reports/definitions/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Dashboards"   "$(get_count reporting/dashboards/by-tenant/$TENANT)"
echo ""
ok "Sample data seeded"
