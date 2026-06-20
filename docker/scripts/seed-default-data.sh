#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# seed-default-data.sh — Comprehensive, idempotent demo data for all 7 services
#
# Safe to run multiple times. Continues on non-fatal errors.
# Pre-requisites: docker-compose up, Keycloak + all services healthy
# ─────────────────────────────────────────────────────────────────────────────
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; NC='\033[0m'
log()  { echo -e "${BLUE}[$(date +%H:%M:%S)]${NC} $*"; }
ok()   { echo -e "${GREEN}  ✓${NC} $*"; }
warn() { echo -e "${YELLOW}  ⚠${NC} $*"; }

# ─── Config ──────────────────────────────────────────────────────────────────
API="http://localhost:8080/api"
TENANT="00000000-0000-0000-0000-000000000001"
HQ_BRANCH="00000000-0000-0000-0000-000000000010"
BR01_ID="00000000-0000-0000-0000-000000000011"
WID="00000000-0000-0000-0000-000000000100"
EMAIL="admin@demo.com"
PASSWORD="Demo123!"

# ─── Helpers ──────────────────────────────────────────────────────────────────
login() {
  LOGIN_RESP=$(/usr/bin/curl -s -m 10 -X POST "$API/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
  TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null)
  if [ -z "$TOKEN" ]; then
    warn "Login failed, trying SSO bridge..."
    KC_TOKEN=$(/usr/bin/curl -s -m 10 -X POST "http://localhost:8180/realms/demo-corp/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password" -d "client_id=report-system-cli" \
      -d "client_secret=report-system-cli-secret-change-me" \
      -d "username=$EMAIL" -d "password=$PASSWORD" \
      | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null)
    SSO_RESP=$(/usr/bin/curl -s -m 10 -X POST -H "Authorization: Bearer $KC_TOKEN" "$API/auth/sso-login")
    TOKEN=$(echo "$SSO_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)
  fi
  if [ -z "$TOKEN" ]; then
    warn "Cannot acquire token — is the system running?"
    return 1
  fi
  echo "$TOKEN"
}

AUTH=()
post() {
  /usr/bin/curl -s -m 10 -o /dev/null -w "%{http_code}" "${AUTH[@]}" -X POST "$1" -d "$2"
}

post_or_warn() {
  local code=$(post "$1" "$2")
  if [[ "$code" =~ ^2 ]]; then
    ok "$3"
  elif [ "$code" = "409" ]; then
    ok "$3 (already exists)"
  else
    warn "$3 — HTTP $code (response: $(/usr/bin/curl -s -m 5 "${AUTH[@]}" -X POST "$1" -d "$2" | head -c 150))"
  fi
}

put_or_warn() {
  local code
  code=$(/usr/bin/curl -s -m 10 -o /dev/null -w "%{http_code}" "${AUTH[@]}" -X PUT "$1" -d "$2")
  if [[ "$code" =~ ^2 ]]; then
    ok "$3"
  else
    warn "$3 — HTTP $code"
  fi
}

psql_exec() {
  docker exec report-postgres psql -U report_user -t -A -c "$1" "$2" 2>/dev/null || true
}

# ─── Phase 1: Login ───────────────────────────────────────────────────────────
log "Logging in as $EMAIL..."
TOKEN=$(login) || exit 1
ok "Token acquired (${#TOKEN} chars)"
AUTH=(-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -H "X-Branch-Id: $HQ_BRANCH")

# ─── Phase 2: Finance — Chart of Accounts ─────────────────────────────────────
log "Finance: Chart of Accounts..."
for acct in \
  '{"code":"1000","name":"Cash","type":"ASSET","active":true,"contra":false}' \
  '{"code":"1100","name":"Accounts Receivable","type":"ASSET","active":true,"contra":false}' \
  '{"code":"1200","name":"Inventory","type":"ASSET","active":true,"contra":false}' \
  '{"code":"1300","name":"Prepaid Expenses","type":"ASSET","active":true,"contra":false}' \
  '{"code":"1400","name":"Fixed Assets","type":"ASSET","active":true,"contra":false}' \
  '{"code":"2000","name":"Accounts Payable","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"2100","name":"Accrued Expenses","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"2200","name":"NSSF Payable","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"2300","name":"Tax Payable","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"2400","name":"Inter-Branch Clearing","type":"LIABILITY","active":true,"contra":false}' \
  '{"code":"3000","name":"Owner Equity","type":"EQUITY","active":true,"contra":false}' \
  '{"code":"4000","name":"Sales Revenue","type":"REVENUE","active":true,"contra":false}' \
  '{"code":"4100","name":"Service Revenue","type":"REVENUE","active":true,"contra":false}' \
  '{"code":"5000","name":"Rent Expense","type":"EXPENSE","active":true,"contra":false}' \
  '{"code":"5100","name":"Salary Expense","type":"EXPENSE","active":true,"contra":false}' \
  '{"code":"5200","name":"Utilities Expense","type":"EXPENSE","active":true,"contra":false}' \
  '{"code":"5300","name":"Cost of Goods Sold","type":"EXPENSE","active":true,"contra":false}' \
  '{"code":"1999-IBC","name":"Inter-Branch Clearing","type":"LIABILITY","active":true,"contra":false}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$acct" "$TENANT")
  name=$(echo "$acct" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')
  post_or_warn "$API/finance/accounts" "$payload" "Account: $name"
done

# ─── Phase 3: Finance — Employees ─────────────────────────────────────────────
log "Finance: Employees..."
for emp in \
  '{"firstName":"Sok","lastName":"Pisey","email":"sok@demo.com","position":"General Manager","baseSalary":2000,"taxDependents":2,"hireDate":"2024-01-15"}' \
  '{"firstName":"Chan","lastName":"Dara","email":"dara@demo.com","position":"Senior Cashier","baseSalary":800,"taxDependents":1,"hireDate":"2024-03-01"}' \
  '{"firstName":"Meas","lastName":"Sreyleak","email":"sreyleak@demo.com","position":"Head Chef","baseSalary":1200,"taxDependents":0,"hireDate":"2024-02-01"}' \
  '{"firstName":"Ly","lastName":"Sophea","email":"sophea@demo.com","position":"Server","baseSalary":500,"taxDependents":3,"hireDate":"2024-06-01"}' \
  '{"firstName":"Heng","lastName":"Ratanak","email":"ratanak@demo.com","position":"Accountant","baseSalary":1000,"taxDependents":1,"hireDate":"2024-04-15"}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$emp" "$TENANT")
  name=$(echo "$emp" | python3 -c 'import sys,json; print(json.load(sys.stdin)["firstName"])')
  post_or_warn "$API/finance/employees" "$payload" "Employee: $name"
done

# ─── Phase 4: Property — 2 Properties + 4 Units + 1 Lease ────────────────────
log "Property: Properties..."
P1=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/property/properties" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"BKK1 Apartments\",\"type\":\"apartment\",\"address\":\"Street 271\",\"city\":\"Phnom Penh\",\"district\":\"Chamkarmon\",\"totalUnits\":3}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))" 2>/dev/null)
if [ -n "$P1" ]; then ok "Property: BKK1 Apartments ($P1)"; else warn "Property 1 failed"; P1=""; fi

P2=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/property/properties" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"BKK1 Office\",\"type\":\"office\",\"address\":\"Street 51\",\"city\":\"Phnom Penh\",\"district\":\"BKK1\",\"totalUnits\":2}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))" 2>/dev/null)
if [ -n "$P2" ]; then ok "Property: BKK1 Office ($P2)"; else warn "Property 2 failed"; P2=""; fi

log "Property: Units..."
for u in "A-101:1:2" "A-102:2:1" "B-201:2:0" "B-202:3:0"; do
  IFS=':' read -r uname floor beds <<< "$u"
  pid="$P1"
  case "$uname" in B-*) pid="$P2";; esac
  if [ -n "$pid" ]; then
    post_or_warn "$API/property/units" \
      "{\"tenantId\":\"$TENANT\",\"propertyId\":\"$pid\",\"label\":\"$uname\",\"floor\":$floor,\"bedrooms\":$beds,\"bathrooms\":1}" \
      "Unit $uname"
  fi
done

# ─── Phase 5: Restaurant — Outlet + Categories + Menu Items ───────────────────
log "Restaurant: Outlet..."
OUTLET=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/restaurant/outlets" \
  -d "{\"tenantId\":\"$TENANT\",\"name\":\"Main Restaurant\",\"address\":\"#15 Norodom Blvd\",\"city\":\"Phnom Penh\",\"type\":\"casual_dining\",\"currency\":\"USD\",\"taxRate\":10}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('id',''))" 2>/dev/null)
if [ -n "$OUTLET" ]; then ok "Outlet: Main Restaurant ($OUTLET)"; else warn "Outlet failed"; OUTLET=""; fi

log "Restaurant: Menu Categories..."
CAT1=""; CAT2=""; CAT3=""
for catdata in '{"name":"Appetizers","nameKh":"ម្ហូបចំហៀង","sortOrder":1,"active":true}' \
               '{"name":"Mains","nameKh":"ម្ហូបដើម","sortOrder":2,"active":true}' \
               '{"name":"Drinks","nameKh":"ភេសជ្ជៈ","sortOrder":3,"active":true}'; do
  cname=$(echo "$catdata" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')
  if [ -n "$OUTLET" ]; then
    payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['outletId']=sys.argv[3]; print(json.dumps(d))" "$catdata" "$TENANT" "$OUTLET")
    cat_id=$(/usr/bin/curl -s -m 10 "${AUTH[@]}" -X POST "$API/restaurant/menu/categories" -d "$payload" \
      | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)
    if [ -n "$cat_id" ]; then
      ok "Category: $cname ($cat_id)"
      case "$cname" in Appetizers) CAT1="$cat_id";; Mains) CAT2="$cat_id";; Drinks) CAT3="$cat_id";; esac
    else
      warn "Category $cname failed"
    fi
  fi
done

log "Restaurant: Menu Items..."
if [ -n "$OUTLET" ] && [ -n "$CAT1" ]; then
  for item in '{"name":"Spring Rolls","nameKh":"ចៀន","description":"Crispy vegetable spring rolls","price":3.50,"categoryId":"'"$CAT1"'","active":true}' \
              '{"name":"Fresh Salad","nameKh":"សាឡាដ","description":"Garden salad with dressing","price":4.00,"categoryId":"'"$CAT1"'","active":true}'; do
    payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['outletId']=sys.argv[3]; print(json.dumps(d))" "$item" "$TENANT" "$OUTLET")
    post_or_warn "$API/restaurant/menu/items" "$payload" "Menu: $(echo "$item" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
  done
fi
if [ -n "$OUTLET" ] && [ -n "$CAT2" ]; then
  for item in '{"name":"Fish Amok","nameKh":"អាម៉ុក","description":"Steamed coconut fish curry","price":8.50,"categoryId":"'"$CAT2"'","active":true}' \
              '{"name":"Beef Lok Lak","nameKh":"ឡុកឡាក់","description":"Stir-fried beef with lime sauce","price":7.50,"categoryId":"'"$CAT2"'","active":true}' \
              '{"name":"Chicken Fried Rice","nameKh":"បាយឆា","description":"Fried rice with chicken and vegetables","price":5.50,"categoryId":"'"$CAT2"'","active":true}'; do
    payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['outletId']=sys.argv[3]; print(json.dumps(d))" "$item" "$TENANT" "$OUTLET")
    post_or_warn "$API/restaurant/menu/items" "$payload" "Menu: $(echo "$item" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
  done
fi
if [ -n "$OUTLET" ] && [ -n "$CAT3" ]; then
  for item in '{"name":"Angkor Beer","nameKh":"បៀរ","description":"Cambodian premium lager","price":2.50,"categoryId":"'"$CAT3"'","active":true}' \
              '{"name":"Fresh Coconut","nameKh":"ទឹកដូង","description":"Chilled fresh coconut water","price":2.00,"categoryId":"'"$CAT3"'","active":true}' \
              '{"name":"Iced Coffee","nameKh":"កាហ្វេទឹកកក","description":"Vietnamese-style iced coffee","price":2.50,"categoryId":"'"$CAT3"'","active":true}'; do
    payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['outletId']=sys.argv[3]; print(json.dumps(d))" "$item" "$TENANT" "$OUTLET")
    post_or_warn "$API/restaurant/menu/items" "$payload" "Menu: $(echo "$item" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
  done
fi

# ─── Phase 6: Inventory — Products, Suppliers, Warehouse ──────────────────────
log "Inventory: Suppliers..."
for sup in \
  '{"name":"Cambodia Beverage Co","contactPerson":"Mr. Long","phone":"012345678","email":"long@cambev.com","paymentTerms":"Net 30","currency":"USD","active":true}' \
  '{"name":"Phnom Penh Supply","contactPerson":"Ms. Sothy","phone":"098765432","email":"sothy@pps.com","paymentTerms":"Net 15","currency":"USD","active":true}' \
  '{"name":"Mekong Foods","contactPerson":"Mr. Vuth","phone":"011222333","email":"vuth@mekong.com","paymentTerms":"Net 30","currency":"USD","active":true}' \
  '{"name":"Seafood Wholesale Co","contactPerson":"Mr. Rithy","phone":"015555666","email":"rithy@seafoodkh.com","paymentTerms":"Net 7","currency":"USD","active":true}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$sup" "$TENANT")
  post_or_warn "$API/inventory/suppliers" "$payload" "Supplier: $(echo "$sup" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

log "Inventory: Products..."
for prod in \
  '{"name":"Jasmine Rice 50kg","sku":"RICE-J50","unitPrice":32.50,"unit":"bag","category":"Grains"}' \
  '{"name":"Cooking Oil 5L","sku":"OIL-C5","unitPrice":8.75,"unit":"bottle","category":"Cooking"}' \
  '{"name":"Fish Sauce 1L","sku":"FISH-1L","unitPrice":3.25,"unit":"bottle","category":"Condiments"}' \
  '{"name":"Chicken Breast 1kg","sku":"CHK-1KG","unitPrice":5.50,"unit":"kg","category":"Meat"}' \
  '{"name":"Garlic 500g","sku":"GRL-500","unitPrice":2.00,"unit":"bag","category":"Vegetables"}' \
  '{"name":"Angkor Beer Case","sku":"BEER-CASE","unitPrice":18.00,"unit":"case","category":"Beverages"}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$prod" "$TENANT")
  post_or_warn "$API/inventory/products" "$payload" "Product: $(echo "$prod" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

log "Inventory: Warehouse..."
psql_exec "INSERT INTO warehouses (id, tenant_id, branch_id, name, type, location, is_active, created_at) \
  VALUES ('$WID', '$TENANT', '$HQ_BRANCH', 'Main Warehouse', 'central', 'Phnom Penh', true, NOW()) \
  ON CONFLICT (id) DO NOTHING;" inventory_db \
  && ok "Warehouse: Main Warehouse" \
  || warn "Warehouse insert failed"

log "Inventory: BR01 branch in inventory_db..."
psql_exec "INSERT INTO branches (id, tenant_id, code, name, branch_type, city, is_active, created_at) \
  VALUES ('$BR01_ID', '$TENANT', 'BR01', 'Branch 01', 'STORE', 'Phnom Penh', true, NOW()) \
  ON CONFLICT (id) DO NOTHING;" inventory_db \
  && ok "Branch BR01 in inventory_db" \
  || warn "Branch BR01 insert failed"

# ─── Phase 7: User → HQ Branch Assignment ─────────────────────────────────────
log "Auth: User-Branch assignment..."
USER_ID=$(/usr/bin/curl -s -m 5 "${AUTH[@]}" "$API/users/me" \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null || echo "")
if [ -n "$USER_ID" ]; then
  post_or_warn "$API/user-branches" \
    "{\"userId\":\"$USER_ID\",\"branchId\":\"$HQ_BRANCH\",\"role\":\"ADMIN\",\"isDefault\":true}" \
    "Admin → HQ branch"
  post_or_warn "$API/user-branches" \
    "{\"userId\":\"$USER_ID\",\"branchId\":\"$BR01_ID\",\"role\":\"MANAGER\",\"isDefault\":false}" \
    "Admin → BR01 branch"
fi

# ─── Phase 8: Payment — Sample Transactions ───────────────────────────────────
log "Payment: Sample transactions..."
for tx in \
  '{"amount":150.00,"currency":"USD","gateway":"cash","method":"CASH","customerName":"Sok Pisey","sourceType":"RENT"}' \
  '{"amount":45.50,"currency":"USD","gateway":"aba","method":"ABA","customerName":"Chan Dara","sourceType":"ORDER"}' \
  '{"amount":2500.00,"currency":"USD","gateway":"aba","method":"BANK_TRANSFER","customerName":"Mekong Foods Co","sourceType":"INVOICE"}' \
  '{"amount":32.00,"currency":"USD","gateway":"wing","method":"WING","customerName":"Meas Sreyleak","sourceType":"ORDER"}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; d['branchId']=sys.argv[3]; print(json.dumps(d))" "$tx" "$TENANT" "$HQ_BRANCH")
  name=$(echo "$tx" | python3 -c 'import sys,json; print(json.load(sys.stdin)["method"])')
  post_or_warn "$API/payment/payments/process" "$payload" "Payment: \$$(echo "$tx" | python3 -c 'import sys,json; print(json.load(sys.stdin)["amount"])') via $name"
done

# ─── Phase 9: Reporting — Reports + Dashboards ────────────────────────────────
log "Reporting: Report Definitions..."
for rpt in \
  '{"name":"Daily Sales Summary","code":"DAILY_SALES","type":"TABULAR","config":"{\"groupBy\":\"day\",\"metrics\":[\"revenue\",\"orders\",\"avgOrderValue\"]}","system":false}' \
  '{"name":"Monthly Revenue","code":"MONTHLY_REV","type":"SUMMARY","config":"{\"groupBy\":\"month\",\"metrics\":[\"revenue\",\"expenses\",\"profit\"]}","system":false}' \
  '{"name":"Property Occupancy","code":"OCCUPANCY","type":"SUMMARY","config":"{\"metrics\":[\"occupied\",\"vacant\",\"rate\"]}","system":false}' \
  '{"name":"Inventory Aging","code":"INV_AGING","type":"TABULAR","config":"{\"groupBy\":\"product\",\"metrics\":[\"quantity\",\"avgAge\"]}","system":false}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$rpt" "$TENANT")
  post_or_warn "$API/reporting/reports/definitions" "$payload" "Report: $(echo "$rpt" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

log "Reporting: Dashboards..."
for d in \
  '{"name":"Executive Dashboard","layout":"{\"widgets\":[{\"type\":\"revenue\",\"position\":0},{\"type\":\"occupancy\",\"position\":1}]}","isDefault":true}' \
  '{"name":"Operations Dashboard","layout":"{\"widgets\":[{\"type\":\"orders\",\"position\":0},{\"type\":\"inventory\",\"position\":1}]}","isDefault":false}'; do
  payload=$(python3 -c "import json,sys; d=json.loads(sys.argv[1]); d['tenantId']=sys.argv[2]; print(json.dumps(d))" "$d" "$TENANT")
  post_or_warn "$API/reporting/dashboards" "$payload" "Dashboard: $(echo "$d" | python3 -c 'import sys,json; print(json.load(sys.stdin)["name"])')"
done

# ─── Summary ──────────────────────────────────────────────────────────────────
echo ""
log "Data Summary:"
get_count() {
  /usr/bin/curl -s -m 5 "${AUTH[@]}" "$API/$1" 2>/dev/null \
    | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "?"
}
printf "  • %-14s %s\n" "Accounts"     "$(get_count finance/accounts/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Employees"    "$(get_count finance/employees/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Properties"   "$(get_count property/properties/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Outlets"      "$(get_count restaurant/outlets/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Menu Items"   "$(get_count restaurant/menu/items/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Suppliers"    "$(get_count inventory/suppliers/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Products"     "$(get_count inventory/products/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Payments"     "$(get_count payment/payments/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Reports"      "$(get_count reporting/reports/definitions/by-tenant/$TENANT)"
printf "  • %-14s %s\n" "Dashboards"   "$(get_count reporting/dashboards/by-tenant/$TENANT)"
echo ""
ok "Default data seeded. Run 'bash docker/scripts/e2e-smoke-test.sh' to verify."
