# AGENTS.md — Report_System

## Architecture

9 Spring Boot 3.2.5 microservices + Eureka + Gateway + Kafka + PostgreSQL-per-service. **Angular 19** frontend (migrated from Nuxt.js 3). All services use **hexagonal (ports & adapters)** architecture.

### Service Port Map

| Service        | Port | Controllers                             |
|----------------|------|-----------------------------------------|
| Auth           | 8081 | Auth, Branch, User, Role, Permission, Tenant, Feature, UserBranch, AdminRealm |
| Property       | 8082 | Property, Unit, Lease, Schedule, Maintenance |
| Restaurant     | 8083 | Outlet, Menu, Order, Customer, Reservation |
| Inventory      | 8084 | Product, Stock, Supplier, PurchaseOrder, StockTransfer |
| Finance        | 8085 | Account, JournalEntry, Invoice, Tax, Employee, Payroll |
| Payment        | 8086 | Payment (tx + reconciliation), PaymentGatewayConfig |
| Reporting      | 8087 | Report, Dashboard                       |
| Delivery       | 8088 | Delivery, Driver, Fleet, Zone, Payout   |
| Realty         | 8089 | Listing, Agent, Offer, Lead, Resident, Tour, Visitor, Parcel, Valuation, HOA, Amenity |

### Gateway Route Rules (`RouteConfig.java`)

- **stripPrefix(1)** for auth routes: `/api/auth/**` → `/auth/**`, `/api/features/**` → `/features/**`
- **stripPrefix(2)** for all 8 domain services: `/api/property/**` → `/properties/**`, `/api/payment/**` → `/payments/**`, `/api/delivery/**` → `/deliveries/**`, `/api/realty/**` → `/listings/**`
- **stripPrefix(2)** for POS: `/api/pos/**` → `/pos/**` (routes to restaurant-service)
- All `lb://` URIs are **lowercase** to match Eureka registration
- Filter order: `OidcTokenValidator` → `TenantContextFilter` → `BranchContextFilter` → route filter

### Keycloak SSO

- Keycloak 24.0.5 at `http://localhost:8180` (dev), master realm admin `admin`/`admin`
- `OidcTokenValidator` validates OIDC JWTs via JWKS (5min cache), falls back to legacy `JwtTokenProvider` for non-OIDC tokens
- Propagates `X-Tenant-Id`, `X-Branch-Id`, `X-User-Id`, `X-User-Email`, `X-User-Name` downstream
- `POST /api/auth/sso-login` bridges Keycloak JWT → legacy JWT (auto-provisions user JIT)

### Multi-Branch

- `branch_id UUID NOT NULL` on every domain table across all 9 services
- `BranchContextFilter` validates `X-Branch-Id` against user's allowed branches (60s cache, email+tenant lookup)
- No branch assignment → tenant-admin mode (full access)
- Frontend: branch service appends `?branchId=` on every API call

## Commands

```bash
# Backend
mvn clean compile                          # Compile all 15 modules (Java 21, Maven 3.9)
mvn test -q                                # Run all tests (48 total: auth 27 + property 21)
mvn test -pl services/property-service -q  # Single service tests
mvn package -DskipTests                    # Build JARs

# Frontend (from frontend/report-system-angular/)
npm run build                              # Angular production build
npm run start                              # Dev server on :4200

# Docker (from project root)
docker/scripts/deploy.sh local up         # Build + start all 20 services (uses --project-directory automatically)
docker/scripts/deploy.sh local down       # Stop all services
docker/scripts/deploy.sh local status     # Show container status
docker/scripts/deploy.sh local logs       # Tail logs

# Docker (manual, from project root)
docker compose -f docker/docker-compose.yml --project-directory . up -d
docker/scripts/seed-sample-data.sh                      # Seed demo data
docker/scripts/e2e-smoke-test.sh                        # 22 integration checks
```

## Known Gotchas

### Frontend
- **API URLs must NOT include `/api` prefix** — the branch service prepends it automatically.
- **`X-Branch-Id`** appended as query param `?branchId=` on every API call via HTTP interceptor (filtered to only API URLs).
- **SSO bridge**: `POST /api/auth/sso-login` with `Authorization: Bearer {keycloak_token}`
- **Dark mode**: PrimeNG dark theme toggle + Tailwind `dark` class on `<html>`
- **Import path depth**: `environments/environment` import depth varies by file location — 3 levels up from `app/core/services/` (`../../../`), 4 levels from `app/features/*/services/` (`../../../../`). Wrong depth is caught at build time.
- **`MessageService`** must be provided at root level (`app.config.ts` providers) or per-component — PrimeNG does NOT set `providedIn: 'root'`. Missing provider causes `NullInjectorError` on toast usage.
- **`<p-dialog>` must NOT be wrapped in `@if`** — wrapping destroys/recreates the component on toggle, losing form state. Use `[(visible)]` binding directly.
- **Keycloak bootstrap** requires `KeycloakService` provider + `APP_INITIALIZER` in `app.config.ts` (was missing, causing `NullInjectorError` on every HTTP request).

### Backend
- **JPA entities with `jsonb` columns** need `@JdbcTypeCode(SqlTypes.JSON)` on the `String` field (13 entities across auth, payment, property, restaurant, reporting)
- **@Table name mismatches** have caused bugs: `accounts` → `chart_of_accounts`, `transactions` → `payment_transactions`
- **Kafka topics**: pre-create them (first send times out in 60s and rolls back otherwise)
- **Gateway `X-Branch-Id` override**: any client-supplied `X-Branch-Id` header or `?branchId=` query param overrides the JWT claim
- **JWT secret**: dev default is `base64EncodedSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm` — change in production

### Testing Pattern
- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ
- Manually construct service in `@BeforeEach` (no Spring Boot test slicing)
- `@DisplayName` on class and methods, `AssertJ` fluent assertions, `ArgumentCaptor` for persisted objects
- Test files go in module at `src/test/java/com/reportsystem/{service}/domain/service/`

### Docker
- Always use `--project-directory .` with docker compose (relative paths resolve from project root, not docker/ dir)
- Use `docker/scripts/deploy.sh local up` for full-stack deployment — handles `--project-directory` automatically
- `init-dbs.sql` is mounted at `/docker-entrypoint-initdb.d/` — postgres runs it only on first DB init (not on restart)
- `docker-compose.prod.yml` has full env vars + health checks
- All Spring Boot services have `start_period: 60s` in health checks; gateway has `start_period: 90s`
- `report-web` depends on `gateway: service_started` (not `service_healthy`) — nginx proxies with retries
- Gateway has `application-docker.yml` with proper `spring.data.redis.host: redis` and Eureka config

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM: 15 modules (4 shared + 2 infra + 9 services) |
| `infrastructure/gateway/.../RouteConfig.java` | All 18 gateway routes with stripPrefix rules |
| `infrastructure/gateway/.../filter/OidcTokenValidator.java` | OIDC JWT validation + legacy fallback |
| `infrastructure/gateway/.../filter/BranchContextFilter.java` | Branch access validation |
| `infrastructure/gateway/src/main/resources/application-docker.yml` | Gateway Docker profile (Redis, Eureka, Keycloak config) |
| `docker/docker-compose.yml` | 20 services (dev) |
| `docker/docker-compose.prod.yml` | 14 services (prod) |
| `docker/scripts/deploy.sh` / `deploy.ps1` | Deploy scripts (local + prod, uses `--project-directory` automatically) |
| `docker/scripts/e2e-smoke-test.sh` | 22-check integration smoke test |
| `docker/scripts/seed-sample-data.sh` | Demo data for all 9 services |
| `frontend/report-system-angular/.../src/environments/` | Angular env config (dev + prod) |
| `frontend/report-system-angular/.../app/core/` | Core services, interceptors, models (nav, auth, branch, pagination) |
| `frontend/report-system-angular/Dockerfile` | Angular nginx Docker build |

## Angular Frontend (Phases 1-7 Complete)

The Angular 19 frontend at `frontend/report-system-angular/` has been scaffolded with:

| Stack | Version |
|-------|---------|
| Angular | 19.2.x (standalone, routing) |
| PrimeNG | 19.1.4 (Aura theme) |
| Tailwind CSS | 4.3.1 (@tailwindcss/postcss) |
| Keycloak Angular | 22.x (keycloak-js 26.x) |
| PrimeIcons | 7.x |

**Project structure** (`src/app/`):
- `core/` — interceptors (auth, branch), services (branch, keycloak-init, nav, auth, pagination, notification, api-error-handler), models (User)
- `shared/` — layout (MainLayout, Sidebar, Topbar)
- `features/` — 10 lazy-loaded modules, all fully built:

  | Module | Components |
  |--------|-----------|
  | Auth | LoginComponent, authGuard |
  | Dashboard | DashboardComponent (8 stat cards covering all modules + quick actions) |
  | Property | PropertyList, PropertyForm, PropertyDetail, UnitList, UnitFormDialog, LeaseList, LeaseFormDialog, MaintenanceList |
  | Restaurant | OutletList, OutletDetail, MenuList, OrderList, CustomerList, ReservationList |
  | Inventory | ProductList, ProductForm, SupplierList, PurchaseOrderList, StockTransferList |
  | Finance | ChartOfAccountsList, AccountForm, JournalEntryList, InvoiceList, InvoiceDetail, InvoiceForm, TaxList, EmployeeList, PayrollList |
  | Payment | PaymentList |
  | Reporting | ReportList (dashboard summary cards + report table) |
  | Delivery | DeliveryList (dispatch), DriverList, FleetList, DeliveryTracking |
  | Realty | ListingList, AgentList, LeadList, Housing (residents) |
  | POS | PosTerminal (order entry), KdsDisplay (kitchen), PosReports |

**Key config**: PrimeNG Aura theme with dark mode via `.dark` class, Tailwind for utility CSS, Keycloak SSO with `check-sso` init, HTTP interceptors for token + tenant/branch headers.

**Docker**: `docker-compose.yml` now includes `report-web` service (nginx, port 4200). Build with `docker compose -f docker/docker-compose.yml up -d --build report-web`.

Backend services also testable via `docker/scripts/e2e-smoke-test.sh` or direct API calls through gateway at `http://localhost:8080`.

## Default Credentials

- Email: `admin@demo.com` / Password: `Demo123!`
- Tenant: `demo-corp` (UUID `00000000-0000-0000-0000-000000000001`)
- HQ Branch UUID: `00000000-0000-0000-0000-000000000010`
- API: `POST http://localhost:8080/api/auth/login`
