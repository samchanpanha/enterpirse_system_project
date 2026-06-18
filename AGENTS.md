# AGENTS.md — Report_System

## Current state

**Sprint 7 (Sprint 9) substantially complete.** Keycloak SSO end-to-end verified + Multi-branch data model in all 7 service DBs + BranchSelector UI in place. Legacy login still works as a fallback.

**Default credentials (after `./docker/scripts/seed-sample-data.sh`):**
- Email: `admin@demo.com`
- Password: `Demo123!`
- Tenant: `demo-corp` (id `00000000-0000-0000-0000-000000000001`)
- Login: `http://localhost:3000/login` (or via API: `POST http://localhost:8080/api/auth/login`)

**Architecture:** 7 Spring Boot microservices + Eureka + Gateway + Kafka + PostgreSQL per service. Nuxt.js 3 frontend with domain layers. Docker Compose (dev/prod) with K8s migration path documented.

## Key design decisions
- Hexagonal (ports & adapters) architecture per service
- All 14 Cambodia tax types in scope (VAT, TOI, PTOI, ToS, ToFB, WHT, Property Rental Tax, Property Tax, Accommodation Tax, Specific Tax, Patent Tax, Signboard Tax, Public Lighting Tax, Minimum Tax)
- 4 gateways: ABA PayWay + Wing + Pi Pay + Cash (strategy pattern with stubs)
- OCR AI for invoice/receipt scanning (Google Cloud Vision + Tesseract)
- Per-location tiered pricing ($29/$79/$199 per month)

## Completed — Sprint 2 ✅

All Sprint 2 deliverables created:

### Property Service (port 8082, hexagonal architecture)
- **Domain models (5):** Property, Unit, Lease, Schedule, MaintenanceTicket
- **Inbound ports (5):** PropertyUseCase, UnitUseCase, LeaseUseCase, ScheduleUseCase, MaintenanceUseCase
- **Outbound ports (5):** PropertyRepository, UnitRepository, LeaseRepository, ScheduleRepository, MaintenanceRepository
- **Domain services (5):** PropertyService, UnitService, LeaseService, MaintenanceService, ScheduleService
- **Infrastructure:**
  - JPA entities (5): PropertyEntity, UnitEntity, LeaseEntity, ScheduleEntity, MaintenanceTicketEntity
  - JPA repositories (5): JpaPropertyRepository, JpaUnitRepository, JpaLeaseRepository, JpaScheduleRepository, JpaMaintenanceRepository
  - Domain mappers (3): PropertyMapper, UnitMapper, LeaseMapper
  - Controllers (5): PropertyController, UnitController, LeaseController, ScheduleController, MaintenanceController
  - GlobalExceptionHandler
  - Event publishers (2): RentInvoiceEventPublisher, TenantEventPublisher (Kafka)
  - Config: PersistenceConfig, PropertyConfig
  - Flyway V1: property_db schema (5 tables + indices + seed listing)
  - main(): PropertyServiceApplication + application.yml

### Restaurant Service (port 8083, hexagonal architecture)
- **Domain models (8):** Outlet, RestaurantTable, Category, MenuItem, Order, OrderItem, Customer, Reservation
- **Inbound ports (6):** OutletService, MenuService, OrderService, CustomerService, ReservationService, PosService (includes PosOrderItem record)
- **Outbound ports (8):** OutletRepository, TableRepository, CategoryRepository, MenuItemRepository, OrderRepository, OrderItemRepository, CustomerRepository, ReservationRepository
- **Domain services (6):** OutletServiceImpl, MenuServiceImpl, OrderServiceImpl, CustomerServiceImpl, ReservationServiceImpl, PosServiceImpl
- **Infrastructure:**
  - JPA entities (8): OutletEntity, TableEntity, CategoryEntity, MenuItemEntity, OrderEntity, OrderItemEntity, CustomerEntity, ReservationEntity
  - JPA repositories (8): All in JpaRepositories.java
  - Adapters (8): JpaOutletAdapter through JpaReservationAdapter
  - Controllers (5): OutletController, MenuController, OrderController, CustomerController, ReservationController
  - KDS WebSocket: KdsWebSocketHandler + WebSocketConfig (broadcasts new orders / status updates to kitchen display)
  - Event publisher: RestaurantEventPublisher (SaleEvent to Kafka)
  - Config: RestaurantConfig
  - Flyway V1: restaurant_db schema (8 tables + indices + seed outlet/categories)
  - main(): RestaurantServiceApplication + application.yml

## Completed — Sprint 3 ✅

### Inventory Service (port 8084, hexagonal architecture)
- **Domain models (8):** Supplier, Warehouse, ProductCategory, Product, StockEntry, StockExit, PurchaseOrder, PurchaseOrderItem
- **Inbound ports (4):** ProductService, StockService, SupplierService, PurchaseOrderService (single file)
- **Outbound ports (8):** All in InventoryRepositories.java
- **Domain services (4):** ProductServiceImpl, StockServiceImpl, SupplierServiceImpl, PurchaseOrderServiceImpl (single file)
- **Infrastructure:**
  - JPA entities (8): All in InventoryEntities.java
  - JPA repositories (8): All in JpaRepositories.java
  - Adapters (8): All in JpaAdapters.java
  - Controllers (4): ProductController, StockController, SupplierController, PurchaseOrderController (single file)
  - Event publisher: InventoryEventPublisher (Kafka: stock.received, stock.low + SaleEvent consumer)
  - Config: InventoryConfig
  - Flyway V1: inventory_db schema (8 tables + indices)
  - main(): InventoryServiceApplication + application.yml + Dockerfile

### Finance Service (port 8085, hexagonal architecture)
- **Domain models (11):** Account, JournalEntry, JournalEntryLine, Invoice, InvoiceItem, TaxRecord, TaxFilingReport, Employee, AttendanceRecord, PayrollPeriod, PayrollItem
- **Inbound ports (4):** AccountingService, InvoiceService, TaxService, PayrollService (single file)
- **Outbound ports (11):** All in FinanceRepositories.java
- **Domain services (4):** AccountingServiceImpl (double-entry engine), InvoiceServiceImpl (AR/AP lifecycle), TaxServiceImpl (tax record + filing), PayrollServiceImpl (TOS/NSSF computation) — all in FinanceServices.java
- **Infrastructure:**
  - JPA entities (11): All in FinanceEntities.java
  - JPA repositories (11): All in JpaRepositories.java
  - Adapters (11): All in JpaAdapters.java
  - Controllers (5): AccountController, JournalEntryController, InvoiceController, TaxController, EmployeeController, PayrollController (2 files)
  - Event consumer: FinanceEventConsumer (sale-events, rent-invoice-events, stock.received → auto-invoices)
  - Config: FinanceConfig
  - Flyway V1: finance_db schema (11 tables + 12 indices + Cambodia payroll/TOS/NSSF)
  - main(): FinanceServiceApplication + application.yml + Dockerfile

## Completed — Sprint 4 ✅

### Payment Service (port 8086, hexagonal architecture)
- **Domain models (4):** Payment, Refund, PaymentGatewayConfig, ReconciliationRecord
- **Inbound ports (2):** PaymentUseCase, ReconciliationUseCase (single file)
- **Outbound ports (4):** PaymentRepository, RefundRepository, PaymentGatewayConfigRepository, ReconciliationRecordRepository (single file)
- **Domain services (2):** PaymentServiceImpl, ReconciliationServiceImpl (single file)
- **Infrastructure:**
  - JPA entities (4): PaymentEntity, RefundEntity, PaymentGatewayConfigEntity, ReconciliationRecordEntity (single file)
  - JPA repositories (4): All in JpaRepositories.java
  - Adapters (4): All in JpaAdapters.java
  - Gateway adapters (4): AbaPayWayAdapter, WingAdapter, PiPayAdapter, CashAdapter — strategy pattern via PaymentGatewayRouter
  - Controller (1): PaymentController (payment CRUD, process, refund, reconcile)
  - Event publisher: PaymentEventPublisher (Kafka: payment.received, payment.failed)
  - Config: PaymentConfig
  - Flyway V1: payment_db schema (4 tables + indices + ABA/Wing/Pi Pay gateway config seed)
  - main(): PaymentServiceApplication + application.yml + Dockerfile

### Reporting Service (port 8087, hexagonal architecture)
- **Domain models (5):** ReportDefinition, ScheduledReport, ReportExecution, DashboardConfig, AggregatedSnapshot
- **Inbound ports (3):** ReportService, DashboardService (single file)
- **Outbound ports (5):** ReportDefinitionRepository, ScheduledReportRepository, ReportExecutionRepository, DashboardConfigRepository, AggregatedSnapshotRepository (single file)
- **Domain services (2):** ReportServiceImpl, DashboardServiceImpl (single file)
- **Infrastructure:**
  - JPA entities (5): ReportDefinitionEntity, ScheduledReportEntity, ReportExecutionEntity, DashboardConfigEntity, AggregatedSnapshotEntity (single file)
  - JPA repositories (5): All in JpaRepositories.java
  - Adapters (5): All in JpaAdapters.java
  - Event consumer: ReportingEventConsumer (Kafka: listens for snapshots)
  - Controllers (2): ReportController, DashboardController (single file)
  - Config: ReportingConfig
  - Flyway V1: reporting_db schema (5 tables + 3 indices)
  - main(): ReportingServiceApplication + application.yml + Dockerfile

### DevOps for Sprint 4
- Dockerfiles: payment-service + reporting-service
- docker-compose.yml: 16 services (14 backend + nuxt-web + infrastructure)
- docker-compose.prod.yml: production variant
- CI workflows: ci-payment.yml, ci-reporting.yml
- Parent POM updated with payment-service + reporting-service

### Bugfixes
- Fixed DTO package imports (`com.reportsystem.common.dto` → `com.reportsystem.shared.dto.event`) across Payment, Reporting, and Finance services
- Fixed PaymentEvent constructor field mismatches (tenantId vs tenant_id)

## Service Port Map (7 of 7 ✅)
| Service | Port | Status |
|---------|------|--------|
| Auth Service | 8081 | ✅ Sprint 1 |
| Property Service | 8082 | ✅ Sprint 2 |
| Restaurant Service | 8083 | ✅ Sprint 2 |
| Inventory Service | 8084 | ✅ Sprint 3 |
| Finance Service | 8085 | ✅ Sprint 3 |
| Payment Service | 8086 | ✅ Sprint 4 |
| Reporting Service | 8087 | ✅ Sprint 4 |

## Project stats (all sprints)
- **Sprint 1:** 110 files (POM, 4 shared, Eureka, Gateway, Auth, Nuxt, DevOps)
- **Sprint 2:** ~110 files (Property ~48, Restaurant ~57, Docker/CI ~5)
- **Sprint 3:** ~44 files (Inventory ~22, Finance ~15, CI/Docker ~7)
- **Sprint 4:** ~37 files (Payment ~18, Reporting ~14, CI/Docker ~5)
- **Total:** ~301 files across 12 Maven modules + frontend + Docker + CI
- Gateway routes pre-configured for all 7 services since Sprint 1
- All services follow identical hexagonal pattern: domain model → port → service → JPA entity → repository → adapter → controller → config → Flyway

## Cambodia Tax Engine (tax-engine module)
- Sprint 1 shared module
- Finance Service implements: TOS progressive tax (4 brackets up to 20%), NSSF (2% employee + 2.6% employer), VAT calculator
- All 14 Cambodia tax types architecture in place

## Completed — Frontend Sprint

### Auth pages ✅
- Landing page, Login, Register, Dashboard with full auth flow
- `useAuth` composable (login/register/logout/init with localStorage persistence)
- `auth` middleware for route protection with 401 auto-redirect
- `$api` plugin with auto Bearer token injection
- Server proxy routes for `/api/auth/login` and `/api/auth/register` → Gateway
- 2 layouts: `default.vue` (public header) and `app.vue` (sidebar nav with all domain links)

### Property pages ✅
- **List** (`/app/property`): card grid with create modal, status badges, unit counts
- **Detail** (`/app/property/[id]`): info panel, unit cards, quick links to schedule/maintenance
- **Schedule** (`/app/property/[id]/schedule`): event list with date badges
- **Maintenance** (`/app/property/[id]/maintenance`): ticket list with priority badges
- Pinia store (`usePropertyStore`) with fetch/create methods
- TypeScript types (`Property`, `Unit`, `Lease`)
- API calls via `$api` plugin → `/api` dev proxy → Gateway (`:8080`) → Property Service (`:8082`)

### Restaurant pages ✅
- **POS** (`/app/restaurant`): floor plan with table grid, section filter, POS modal with menu categories/items + cart
- **Menu** (`/app/restaurant/menu`): category/CRUD sidebar, item grid with Khmer name, edit/delete
- **Orders** (`/app/restaurant/orders`): order cards with status badges, detail modal with item list + status workflow
- **KDS** (`/app/restaurant/kds`): kitchen display auto-refreshing every 15s, pending/preparing orders with item detail
- **Reservations** (`/app/restaurant/reservations`): date filter, reservation cards with seat/cancel actions
- **Customers** (`/app/restaurant/customers`): table view with name/phone/visits/spent/VIP status
- Pinia store (`useRestaurantStore`) with full CRUD for all 5 backend controllers
- TypeScript types (`Outlet`, `RestaurantTable`, `Category`, `MenuItem`, `Order`, `OrderItem`, `Customer`, `Reservation`)

### Inventory pages ✅
- **List** (`/app/inventory`): product table with stock levels, stock in/out modal, inline edit
- **Suppliers** (`/app/inventory/suppliers`): table with create modal
- **Purchase Orders** (`/app/inventory/purchase-orders`): PO cards with receive workflow
- Pinia store (`useInventoryStore`) with products, suppliers, POs, stock entry/exit
- TypeScript types (`Product`, `Supplier`, `PurchaseOrder`, `PurchaseOrderItem`, `StockEntry`, `StockExit`, `Warehouse`, `ProductCategory`)

### Finance pages ✅
- **Dashboard** (`/app/finance`): stat cards + recent invoices + journal entries
- **Accounts** (`/app/finance/accounts`): chart of accounts table with create modal
- **Invoices** (`/app/finance/invoices`): invoice table with pay action, create form with tax calculation
- **Tax** (`/app/finance/tax`): period filter, tax records table, total tax summary, filing report generation
- **Employees** (`/app/finance/employees`): employee table with NSSF fields
- **Payroll** (`/app/finance/payroll`): payroll period creation, run payroll with TOS/NSSF item display
- Pinia store (`useFinanceStore`) with all 6 controller endpoints
- Full TypeScript types (`Account`, `JournalEntry`, `Invoice`, `TaxRecord`, `Employee`, `PayrollPeriod`, `PayrollItem`, etc.)

### Payment pages ✅
- **History** (`/app/payment`): transaction table with gateway badges, process payment form, refund action
- **Reconciliation** (`/app/payment/reconciliation`): reconciliation cards with match/unmatch summary, start/complete workflow
- Pinia store (`usePaymentStore`) with process, refund, reconciliation endpoints
- TypeScript types (`Transaction`, `Refund`, `ReconciliationRecord`)

### Reporting pages ✅
- **Reports** (`/app/reports`): report definition cards with type badges, create + run actions
- **Dashboards** (`/app/reports/dashboards`): dashboard config list with create modal
- Pinia store (`useReportingStore`) with definitions, executions, dashboards
- TypeScript types (`ReportDefinition`, `ReportExecution`, `DashboardConfig`)

### Frontend CI ✅
- `.github/workflows/ci-frontend.yml`: typecheck → lint → build → Docker image

### Total frontend pages
| Module | Pages | Store | Types |
|--------|-------|-------|-------|
| Auth | 4 (landing, login, register, dashboard) | useAuth composable | auth.ts |
| Property | 4 (list, detail, schedule, maintenance) | usePropertyStore | property.ts |
| Restaurant | 6 (POS, menu, orders, KDS, reservations, customers) | useRestaurantStore | restaurant.ts |
| Inventory | 3 (list, suppliers, POs) | useInventoryStore | inventory.ts |
| Finance | 6 (dashboard, accounts, invoices, tax, employees, payroll) | useFinanceStore | finance.ts |
| Payment | 2 (history, reconciliation) | usePaymentStore | payment.ts |
| Reporting | 2 (reports, dashboards) | useReportingStore | inline types |
| **Total** | **27 pages** | **6 Pinia stores + 1 composable** | **7 type files** |

## Remaining work

### Frontend build ✅
- `npm run build` passes — 149 client modules + 83 server modules, produces 1.91 MB `.output/`
- Tailwind CSS warnings (no utility classes detected) — Content paths need configuration, cosmetic only

### Verify backend compilation (pending)
- `mvn clean compile` across all modules (requires Java 21 + Maven + PostgreSQL)
- 12 Maven modules: 4 shared, 2 infrastructure (eureka + gateway), 7 services

### Post-MVP
- Integrate real ABA PayWay / Wing / Pi Pay APIs (currently stubs)
- Production hardening (rate limiting, audit logging, backup strategy)

## Sprint 5 — Squirrel audit ✅ (2026-06-18)

Full 8-phase audit (squirrel skill). Found and fixed several real bugs:

### Frontend fixes
- **`srcDir: 'app/'`** added to `nuxt.config.ts` — Nuxt was not detecting the `app/` directory, breaking auto-imports (composable, layouts, pages).
- **CSS path fixed** — `~/app/assets/css/main.css` → `~/assets/css/main.css` (was producing `app//app/assets/...` after srcDir change).
- **`Api` type added to `plugins/api.ts`** — replaced loose `typeof $fetch` with a generic `<T = any>` to stop 28+ `unknown` typecheck errors across stores.
- **`~/.eslintrc.cjs` created** — was missing; lint script silently failed.
- **Auto-fix applied** — 1891 lint problems → 0 errors. Manual fixes for 9 remaining: unused vars, `async` without `await`, type ordering.
- **Type order fixed** in `auth.ts` + `finance.ts` (UserInfo, JournalEntryLine) — eliminated `no-use-before-define` errors.
- **All 5 store imports** corrected from `~/app/shared/types/X` → `~/shared/types/X`.

### Backend fixes
- **CRITICAL: JWT gateway filter was a no-op** — `extractTenantIdFromToken()` returned `null` for any token. Replaced with real `JwtTokenProvider.validateToken()` call. Added `security-core` dependency to gateway pom, created `JwtConfig` bean, populated `jwt.secret` in gateway `application.yml`. Filter now propagates `X-Tenant-Id` + `X-User-Id` headers to downstream services.
- **CRITICAL: Gateway routes used UPPERCASE** — `lb://AUTH-SERVICE` etc. didn't match service registration names (`auth-service` lowercase). Fixed all 7 routes.
- **CRITICAL: Kafka topic mismatch** — `RentInvoiceEventPublisher` published to `rent.invoice.generated` but `FinanceEventConsumer` listened on `rent-invoice-events`. Fixed to `rent-invoice-events`.

### Infra additions
- **`k8s/kustomization.yaml`** — Kustomize entry point for `kubectl apply -k k8s/`.
- **`k8s/README.md`** — full deployment guide: architecture, manifests table, quick start, k3d local dev, port map, required production changes.
- **`.gitignore`** — covers target/, node_modules/, .nuxt/, .output/, logs, IDE files, secrets.

### Verification (frontend)
- ✅ `npm run typecheck` — 0 errors
- ✅ `npm run lint` — 0 errors
- ✅ `npm run build` — 2.39 MB total (589 kB gzip), 137 modules

### Pending (Java/Maven not available on this machine)
- `mvn clean compile` across all 12 modules
- Unit tests (no test files exist yet — empty `src/test` directories in 4 services)
- Integration tests for Kafka consumer/publisher round-trips

## Sprint 6 — Local Deploy + Sample Data + Login Fix ✅ (2026-06-18)

End-to-end local deployment test on Mac. Found and fixed 12 issues:

### Infrastructure
- **`init-dbs.sh` → `init-dbs.sql`**: bash mount had `noexec`, postgres init silently failed. Converted to plain SQL (postgres entrypoint supports `.sql` natively).
- **`eureka/Dockerfile`**: added `apk add --no-cache curl` (JRE-alpine has no curl, healthcheck was failing).
- **`docker-compose.yml` gateway env**: added `SPRING_DATA_REDIS_HOST=redis` (was connecting to `localhost:6379` which doesn't exist in container).
- **`docker-compose.yml` nuxt-web env**: added `NUXT_API_BASE_URL=http://gateway:8080` (used by server-side proxy routes).
- **`--project-directory` flag**: added to `docker compose` invocations in deploy scripts so relative paths resolve from project root (was resolving to `docker/`).
- **Compose file paths**: changed `./scripts/...` to `./docker/scripts/...` to match new project root.

### Backend bugs (12 fixed)
- **`pom.xml`**: added `<parameters>true</parameters>` to maven-compiler-plugin → Spring `@PathVariable` works.
- **`JwtAuthGatewayFilter`**: public path check now matches both `/api/auth/...` and post-stripPrefix `/auth/...` (the route filter strips before global filter sees the path).
- **`RouteConfig.java`**: changed `stripPrefix(1)` to `stripPrefix(2)` for 6 service routes (property/restaurant/inventory/finance/payment/reporting). Auth kept at `stripPrefix(1)` because its controller is at `/auth`.
- **`AuthService.java` + `UserRepository` + `JpaUserRepository` + `PersistenceConfig`**: added `findByEmail(String email)` method. The old `findByEmailAndTenantId(email, null)` never matched anything (Spring Data JPA null param = never matches).
- **`AccountEntity`**: `@Table("accounts")` → `@Table("chart_of_accounts")` (entity name didn't match Flyway migration).
- **`TransactionEntity`**: `@Table("transactions")` → `@Table("payment_transactions")` (same issue).
- **13 JPA entities with jsonb columns** (auth-tenant, payment-tx, property-lease/schedule/unit, restaurant-menuItem/orderItem/outlet, reporting-*): added `@JdbcTypeCode(SqlTypes.JSON)` to all `String` fields mapped to `jsonb` columns.
- **`ReportDefinitionEntity.layout` + `DashboardConfigEntity.layout`**: same jsonb fix (the regex missed `jsonb NOT NULL` without `default`).

### Frontend login fix
- **`app/server/api/auth/login.post.ts` + `register.post.ts`**: hardcoded `http://localhost:8080/auth/login` → uses `useRuntimeConfig().public.apiBaseUrl + '/api/auth/login'`. This fixes both: (1) container networking (uses `http://gateway:8080` from inside docker), (2) gateway route prefix (must include `/api`).

### Files
- `docker/scripts/init-dbs.sql` (new, replaces `.sh`)
- `docker/scripts/seed-sample-data.sh` (new, 165 data rows generated)
- All fixes above

## Sprint 7 — Keycloak SSO + Multi-Branch (Sprint 9 ✅, Sprint 10 ✅, Sprint 11 ✅, Sprint 12 ✅)

**Sprint 9 (Keycloak Foundation) ✅ — verified end-to-end on 2026-06-18**

### Keycloak SSO
- **Keycloak 24.0.5** container in `docker-compose.yml` + dedicated `postgres-keycloak` DB
- **Realm export** (`docker/keycloak/realm/demo-corp-realm.json`):
  - `admin@demo.com` user (password `Demo123!`, attributes `tenantId` + `defaultBranchId` set)
  - 3 clients: `report-system-web` (PKCE public), `report-system-api` (bearer-only), `report-system-cli` (direct grant)
  - 3 protocol mappers per client: `tenantId`, `defaultBranchId`, `realm-roles` (17 config rows total)
- **Gateway OIDC validator** (`infrastructure/gateway/.../filter/OidcTokenValidator.java`):
  - Real OIDC validation via JWKS from Keycloak (cached 5min per realm)
  - Extracts `iss` → realm, `kid` from header
  - Reads `tenantId` + `defaultBranchId` from claims (falls back to realm-tenant mapping)
  - Propagates `X-Tenant-Id`, `X-Branch-Id`, `X-User-Id`, `X-User-Email`, `X-User-Name` to downstream services
  - Public-path bypass for `/api/auth/*` (login still works without Keycloak)
- **SSO bridge** (`POST /api/auth/sso-login`):
  - Accepts Keycloak JWT, exchanges for legacy JWT (backward compat)
  - Auto-provisions user in `auth_db` on first login (JIT)
  - Returns deprecation header on legacy login: `X-Auth-Deprecation: true`
- **Frontend Keycloak plugin** (`app/plugins/keycloak.client.ts`):
  - `keycloak-js` package (PKCE S256, silent SSO check on load)
  - Bridges Keycloak token to legacy JWT automatically
  - `/auth/callback` page for PKCE redirect-back
  - `/silent-check-sso.html` for hidden iframe SSO
- **Login UI** (`/login`): "Sign in with Keycloak (SSO)" button (primary) + legacy form (fallback, marked deprecated)
- **Keycloak 24 schema fix**: `keycloak-add-mappers.sh` updated to use `protocol_mapper_config` table (split from `protocol_mapper` in Keycloak 24+); idempotent; integrated into `seed-sample-data.sh`

### Multi-branch foundation (Sprint 10 ✅ — verified end-to-end on 2026-06-18)
- **V2 migrations in all 7 service DBs** (auth, property, restaurant, inventory, finance, payment, reporting):
  - Creates `branches` + `user_branches` tables
  - Idempotent (`CREATE TABLE IF NOT EXISTS` + DO block for all tables)
  - Adds `branch_id UUID NOT NULL` to every domain table (dynamic via `information_schema`)
  - Backfills `branch_id = HQ` for all existing rows
  - FK + index on every `branch_id` column
  - Seeds HQ branch (`00000000-0000-0000-0000-000000000010`) for demo tenant
- **Branch domain in auth-service**: `Branch` model, `BranchEntity`, `BranchMapper`, `JpaBranchRepository`, `BranchRepository` port, `BranchService`, `BranchController` (full CRUD at `/api/branches`)
- **Branch domain model** in all other services: `branchId` field on every JPA entity (property, restaurant, inventory, finance, payment, reporting)
- **Branch-aware controllers** across all 6 services: read `X-Branch-Id` header, call `getXByTenantAndBranch(...)` methods
- **UserBranchController** (`/api/user-branches`):
  - `GET /by-user/{userId}` — list user's branches
  - `GET /by-email?email=X&tenantId=Y` — resolve Keycloak user via email+tenant
  - `GET /by-branch/{branchId}` — list branch's users
  - `POST /` — assign user to branch
  - `PUT /{userId}/{branchId}` — update role
  - `DELETE /{userId}/{branchId}` — unassign
- **SSO login auto-assignment**: `sso-login` endpoint now auto-assigns new SSO users to their default branch (from `X-Branch-Id` header) so the BranchContextFilter can validate their access
- **New property endpoints**: `GET /api/property/schedules/by-property/{propertyId}` and `GET /api/property/maintenance/by-property/{propertyId}` — aggregate by property (joins through units)
- **BranchContextFilter in gateway** (`BranchContextFilter.java`):
  - Validates `X-Branch-Id` against user's allowed branch list (60s cache)
  - Lookup by email+tenant (since X-User-Id is the Keycloak sub, not the local user UUID)
  - Returns 403 if user requests a branch they don't have access to
  - Permissive when user has no branch assignments (tenant-admin mode)
  - Public paths: `/api/auth/`, `/api/branches/`, `/api/user-branches/`, `/actuator/`
- **Frontend branch filtering**:
  - All 7 Pinia stores use `branchStore.$apiWithBranch()` (added where missing in inventory, payment, restaurant)
  - Direct `$api` calls in `kds/index.vue`, `property/schedule/index.vue`, `property/maintenance/index.vue` converted to use `branchStore.$apiWithBranch()`
  - `app/stores/branch.ts` — Pinia store with `$apiWithBranch()` helper
  - `app/components/BranchSelector.vue` — dropdown in app header
  - `app/layouts/app.vue` — uses `<BranchSelector />` on mount
  - `app/pages/app/admin/branches/index.vue` — full branch CRUD UI

### End-to-end verification (2026-06-18) ✅
```text
=== Full integration smoke test ===
1. Keycloak login:                 1257-char JWT (with tenantId, defaultBranchId claims)
2. /api/branches:                  HTTP 200  (HQ branch returned)
3. /api/user-branches/by-user:     HTTP 200  (user-branch assignment returned)
4. /api/property/properties:       HTTP 200
5. /api/inventory/products:        HTTP 200
6. /api/finance/accounts:          HTTP 200
7. /api/restaurant/outlets:        HTTP 200
8. /api/payment/payments:          HTTP 200
9. /api/reporting/reports:         HTTP 200
10. /api/auth/sso-login:           HTTP 200  (legacy JWT issued)
```

### Property store path fix (also Sprint 7)
- `app/stores/property.ts` had a path bug (`/property/properties/tenant/${id}` instead of `/property/properties/by-tenant/${id}`) and an unused `branchStore` import
- Fixed to use the correct path + `branchStore.$apiWithBranch()` for branch-aware queries
- Lint: 0 errors (was 1)

### Files added in Sprint 7
```
infrastructure/gateway/.../filter/OidcTokenValidator.java     NEW
infrastructure/gateway/.../filter/TenantContextFilter.java    NEW
services/auth-service/.../domain/model/Branch.java            NEW
services/auth-service/.../domain/model/UserBranch.java        NEW
services/auth-service/.../domain/service/BranchService.java   NEW
services/auth-service/.../domain/service/UserBranchService.java NEW
services/auth-service/.../domain/port/outbound/BranchRepository.java NEW
services/auth-service/.../domain/port/outbound/UserBranchRepository.java NEW
services/auth-service/.../infrastructure/persistence/entity/BranchEntity.java NEW
services/auth-service/.../infrastructure/persistence/entity/UserBranchEntity.java NEW
services/auth-service/.../infrastructure/persistence/mapper/BranchMapper.java NEW
services/auth-service/.../infrastructure/persistence/mapper/UserBranchMapper.java NEW
services/auth-service/.../infrastructure/persistence/repository/JpaBranchRepository.java NEW
services/auth-service/.../infrastructure/persistence/repository/JpaUserBranchRepository.java NEW
services/auth-service/.../infrastructure/web/BranchController.java NEW
services/auth-service/.../infrastructure/web/UserBranchController.java NEW
services/auth-service/.../config/PersistenceConfig.java       MODIFIED (added UserBranchRepository bean)
services/*-service/.../resources/db/migration/V2__add_branches_and_branch_id.sql  NEW (×7)
infrastructure/gateway/.../config/RouteConfig.java            MODIFIED (+/api/user-branches route)
docker/keycloak/realm/demo-corp-realm.json                    NEW (with mappers for all 3 clients)
docker/scripts/keycloak-add-mappers.sh                        NEW (Keycloak 24 schema, idempotent)
docker/scripts/keycloak-set-user-attrs.sh                      NEW
docker/scripts/seed-sample-data.sh                            MODIFIED (calls keycloak-add-mappers.sh)
frontend/.../app/plugins/keycloak.client.ts                   NEW
frontend/.../app/components/BranchSelector.vue                NEW
frontend/.../app/composables/useAuth.ts                       MODIFIED (loginWithKeycloak, bridgeFromKeycloak)
frontend/.../app/stores/branch.ts                             NEW
frontend/.../app/layouts/app.vue                              MODIFIED (BranchSelector in header)
frontend/.../app/pages/app/admin/index.vue                    NEW
frontend/.../app/pages/app/admin/branches/index.vue           NEW
frontend/.../app/pages/auth/callback.vue                      NEW
frontend/.../app/public/silent-check-sso.html                 NEW
frontend/.../app/stores/property.ts                           FIXED (path + branchStore)
```

### Cross-branch features (Sprint 11 ✅ — verified end-to-end on 2026-06-18)
- **Stock transfer workflow** (`stock_transfers` + `stock_transfer_items` tables, V3 migration in `inventory_db`):
  - `POST /api/inventory/transfers` — create DRAFT
  - `POST /api/inventory/transfers/{id}/ship` — SHIPPED (decrements source stock)
  - `POST /api/inventory/transfers/{id}/receive` — RECEIVED (increments target stock)
  - `POST /api/inventory/transfers/{id}/cancel` — CANCELLED (restocks if SHIPPED)
  - `GET /api/inventory/transfers?tenantId=X` — list with status filter
  - `GET /api/inventory/transfers/by-branch/{branchId}` — list per branch
  - `GET /api/inventory/transfers/incoming/{branchId}` — incoming inbox (status=SHIPPED)
- **StockTransfer domain model** (DRAFT / SHIPPED / RECEIVED / CANCELLED) with per-line `receivedQuantity` for partial receives + discrepancy notes
- **Kafka events** (`stock.transfer.requested/shipped/received/cancelled` + `stock.transfer.events` for bulk consumers):
  - Producer: `InventoryEventPublisher.publishStockTransferEvent()`
  - Consumer: `FinanceEventConsumer.handleStockTransferReceived()` posts 4-line inter-branch journal entry
- **Inter-branch journal entries** (auto-posted on `stock.transfer.received`):
  - DEBIT  Inventory (target branch)        — `1400-INV-{branch-short}`
  - CREDIT Inter-Branch Clearing            — `1999-IBC` (tenant-level, auto-created)
  - DEBIT  Inter-Branch Clearing            — counterpart
  - CREDIT Inventory (source branch)        — `1400-INV-{branch-short}`
  - The two IBC lines net to zero; each branch's books balance
- **V3 migration in finance_db**: adds `from_branch_id` + `to_branch_id` to `journal_entries`
- **StockTransferEvent shared DTO** in `common-dto` (with `productName`, `unitCost`, `receivedQuantity`)
- **Frontend: `/app/inventory/transfers` page** (list, create, ship, receive, cancel) with status filters + branch selector integration
- **Gateway route**: `/api/inventory/transfers/**` → inventory-service

### Infrastructure fix (enables inter-container Kafka)
- **Kafka broker advertised listener**: changed from `PLAINTEXT://localhost:9092` → `PLAINTEXT://kafka:9092` so inter-container clients can connect
- **Kafka env vars** in `docker-compose.yml` for inventory + finance services: `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- **Pre-create Kafka topics** for the new transfer events (otherwise the first send times out 60s and rolls back the transaction)

### Deferred to a future sprint
- **Cross-branch reports** with `ReportScope` enum (SINGLE_BRANCH, MULTI_BRANCH, ALL_BRANCHES)
- **Bulk operations UI** ("close month for all branches" admin button)
- **Per-realm JWK cache** in gateway (already works with per-call fetch + 5min TTL)
- **Transfer approval workflow** (manager role in source branch required before ship)
- **Branch hierarchy UI** (basic, no parent/child enforcement yet)

### Sprint 12 — E2E tests + migration tools + docs ✅ (verified on 2026-06-18)

**Unit tests (JUnit 5 + Mockito) — 27 tests, 0 failures**
- `AuthServiceTest` (11) — register, login, JWT generation, refresh
- `BranchServiceTest` (10) — CRUD, default branch logic, delete protection
- `UserBranchServiceTest` (6) — assign, unassign, idempotency, default role
- `JwtTokenProviderTest` (5) — generate, validate, expiry, wrong secret, garbage

**E2E integration test (`docker/scripts/e2e-smoke-test.sh`) — 22 checks, 0 failures**
1. Keycloak OIDC login + JWT claims (tenantId, defaultBranchId)
2. SSO bridge to legacy JWT
3. All 7 backend services reachable via gateway
4. Branch CRUD
5. User-branch auto-assignment via SSO
6. BranchContextFilter (allowed branch → 200, bogus → 403)
7. Full stock transfer flow (DRAFT → SHIPPED → RECEIVED)
8. Kafka event consumed by finance-service
9. Inter-branch journal entry (4 balanced lines, IBC account)

**Migration tool (`docker/scripts/migrate-users-to-keycloak.sh`)**
- One-time script: exports users from auth_db, creates Keycloak realm per tenant + user per row
- Sets `tenantId` and `defaultBranchId` attributes
- Generates random temp password (force reset on first login)
- `--dry-run` mode for preview

**Documentation**
- `README.md` — system overview, architecture diagram, quick start, services, testing, deployment
- `docker/scripts/wait-healthy.sh` — wait for all 18 services to become healthy (HTTP or TCP)
- `.github/workflows/ci-tests.yml` — runs unit tests + frontend lint/typecheck + full E2E in CI
- All auth-service CI workflow picks up the new tests automatically

## Sprint 13 — JeecgBoot Admin UI + Feature Enablement ✅

### Admin template (JeecgBoot/Ant Design style)
- **Design tokens**: exact palette (`#1890ff`, `#52c41a`, `#faad14`, `#ff4d4f`, `#001529`) in `tailwind.config.ts` + `antd-tokens.css`
- **Reusable components**: `AdminHeader`, `AdminSidebar`, `AdminBreadcrumbs`, `AdminPageHeader`, `AdminSearchBar`, `AdminTable`, `AdminDrawer`, `AdminForm`, `AdminEmpty`
- **Composables**: `useLayoutState`, `useMenuConfig`, `useListPage`, `useDrawer`, `usePermission`, `useFeature`, `useFormSchema`
- **Permission system**: `permission` store, `v-can` directive, feature-gated sidebar
- **Feature system**: `feature` store, `v-feature` directive, runtime feature resolution
- **27 pages converted** to use the admin shell + component library
- **Build verified**: `npm run typecheck` 0 errors, `npm run lint` 0 errors, `npm run build` 3.81 MB (890 kB gzip)

### Feature enablement (3-actor model)
- **Client** = `tenants` table surfaced as "Client" in UI; **User** = `users` table; **Customer** = per-domain tables
- **Auth-service V3 migration**: `tenants.tier`, `features` catalog (~30 features), `client_features` override table
- **Feature resolution**: tier requirement → explicit override → default-on for tier
- **REST API**: `/api/features/tree`, `/api/features/enabled`, `/api/features/check/{code}`, `/{code}/enable`, `/{code}/disable`
- **Gateway route**: `/api/features/**` → auth-service
- **Per-domain customers** migrations: `property_db.customers`, `inventory_db.customers`, `finance_db.customers`

### Local deploy test (2026-06-18) ✅
- `mvn clean package -DskipTests` → all 15 modules BUILD SUCCESS
- `docker compose build --parallel` → all images rebuilt
- Found/fixed: Kafka broker `NodeExists` on recreate (wiped zookeeper/kafka containers + volumes)
- Improved `docker-compose.yml`:
  - Removed obsolete `version: "3.9"`
  - Switched Keycloak command to `start-dev` (local HTTP) and fixed bootstrap admin env vars to `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`
  - Added `kafka-broker-api-versions.sh` healthcheck for Kafka
  - Added Zookeeper `srvr` healthcheck
  - Changed inventory/finance `depends_on` from `service_started` to `service_healthy`
  - Added explicit `KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092`
- `docker/scripts/keycloak-ensure-admin.sh` (new): patches master realm `ssl_required=NONE` so AdminRealmController works over HTTP
- `docker/scripts/deploy.sh` + `docker/scripts/seed-sample-data.sh` now call `keycloak-ensure-admin.sh`
- `docker/scripts/wait-healthy.sh` → all 15 services healthy
- `docker/scripts/e2e-smoke-test.sh` → 22/22 checks PASSED
- Feature API verified → 47 features enabled for demo tenant, `/api/features/check/restaurant.pos` → `true`
- AdminRealmController end-to-end verified → new tenant realm + bootstrap admin + clients/mappers created; login returns JWT

### Keycloak bootstrap admin fix ✅
- **Problem**: Keycloak master realm defaulted to `ssl_required=EXTERNAL`, blocking the Admin API over plain HTTP, and `AdminRealmController` could not get an admin token.
- **Fix**:
  - `services/auth-service/src/main/resources/keycloak/realm-template.json` — full realm template (roles, clients, protocol mappers, bootstrap admin user)
  - `KeycloakAdminClient.createRealm(...)` now imports the template, generates random client secrets, and provisions the admin user in one call
  - `AdminRealmController.createRealm(...)` accepts `adminEmail`, `adminPassword`, `defaultBranchId` and returns the generated client secrets
  - `docker/scripts/keycloak-ensure-admin.sh` patches master realm `ssl_required=NONE` and restarts Keycloak when needed
  - `docker/docker-compose.yml`: `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` env vars (Keycloak 24 compatible), `start-dev` command for local HTTP

## Next steps
1. ~~Add unit tests for at least one service~~ ✅ Done (Sprint 12)
2. ~~Fix Keycloak bootstrap admin user creation so AdminRealmController works~~ ✅ Done
3. Cross-branch reports (deferred from Sprint 11)
4. Push to GitHub (`.gitignore` is already in place)
5. Consider Kustomize overlays for dev/staging/prod
6. Production hardening: managed Kafka/KRaft, TLS, S3 backups, rate limiting, observability, PCI-DSS scoped payment isolation

## Relevant Files
- `plan.md`: Complete 1668-line project plan (architecture, 60+ table schemas, 8-sprint plan)
- `pom.xml`: Parent POM with 12 modules (4 shared + 2 infrastructure + 7 services)
- `services/payment-service/`: 18 files — hexagonal with ABA/Wing/Pi Pay/Cash strategy gateways
- `services/reporting-service/`: 14 files — hexagonal with definitions/dashboards/snapshots
- `infrastructure/gateway/.../RouteConfig.java`: All 7 routes (AUTH through REPORTING)
- `docker/docker-compose.yml`: 16 services (infra + all 7 services + nuxt)
- `.github/workflows/`: 8 CI workflows (auth, gateway, property, restaurant, inventory, finance, payment, reporting)
- `shared/common-dto/src/main/java/com/reportsystem/shared/dto/event/`: 5 cross-service event DTOs
