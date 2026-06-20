# AGENTS.md — Report_System

## Architecture

7 Spring Boot 3.2.5 microservices + Eureka + Gateway + Kafka + PostgreSQL-per-service. **Angular 19** frontend (migrated from Nuxt.js 3). All services use **hexagonal (ports & adapters)** architecture.

### Service Port Map

| Service        | Port | Controllers                             |
|----------------|------|-----------------------------------------|
| Auth           | 8081 | Auth, Branch, User, Role, Permission, Tenant, Feature, UserBranch, AdminRealm |
| Property       | 8082 | Property, Unit, Lease, Schedule, Maintenance |
| Restaurant     | 8083 | Outlet, Menu, Order, Customer, Reservation |
| Inventory      | 8084 | Product, Stock, Supplier, PurchaseOrder, StockTransfer |
| Finance        | 8085 | Account, JournalEntry, Invoice, Tax, Employee, Payroll |
| Payment        | 8086 | Payment (tx + reconciliation)           |
| Reporting      | 8087 | Report, Dashboard                       |

### Gateway Route Rules (`RouteConfig.java`)

- **stripPrefix(1)** for auth routes: `/api/auth/**` → `/auth/**`, `/api/features/**` → `/features/**`
- **stripPrefix(2)** for all 6 domain services: `/api/property/**` → `/properties/**`, `/api/payment/**` → `/payments/**`
- All `lb://` URIs are **lowercase** to match Eureka registration
- Filter order: `OidcTokenValidator` → `TenantContextFilter` → `BranchContextFilter` → route filter

### Keycloak SSO

- Keycloak 24.0.5 at `http://localhost:8180` (dev), master realm admin `admin`/`admin`
- `OidcTokenValidator` validates OIDC JWTs via JWKS (5min cache), falls back to legacy `JwtTokenProvider` for non-OIDC tokens
- Propagates `X-Tenant-Id`, `X-Branch-Id`, `X-User-Id`, `X-User-Email`, `X-User-Name` downstream
- `POST /api/auth/sso-login` bridges Keycloak JWT → legacy JWT (auto-provisions user JIT)

### Multi-Branch

- `branch_id UUID NOT NULL` on every domain table across all 7 services
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

# Docker (from project root, NOT docker/)
docker compose -f docker/docker-compose.yml up -d       # Must use --project-directory .
docker/scripts/seed-sample-data.sh                      # Seed demo data
docker/scripts/e2e-smoke-test.sh                        # 22 integration checks
```

## Known Gotchas

### Frontend
- **API URLs must NOT include `/api` prefix** — the branch service prepends it automatically.
- **`X-Branch-Id`** appended as query param `?branchId=` on every API call via HTTP interceptor.
- **SSO bridge**: `POST /api/auth/sso-login` with `Authorization: Bearer {keycloak_token}`
- **Dark mode**: PrimeNG dark theme toggle + Tailwind `dark` class on `<html>`

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
- `init-dbs.sql` is mounted at `/docker-entrypoint-initdb.d/` — postgres runs it only on first DB init (not on restart)
- `docker-compose.prod.yml` has full env vars + health checks (was missing both, now fixed)

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM: 12 modules (4 shared + 2 infra + 7 services) |
| `infrastructure/gateway/.../RouteConfig.java` | All 16 gateway routes with stripPrefix rules |
| `infrastructure/gateway/.../filter/OidcTokenValidator.java` | OIDC JWT validation + legacy fallback |
| `infrastructure/gateway/.../filter/BranchContextFilter.java` | Branch access validation |
| `docker/docker-compose.yml` | 16 services (dev) |
| `docker/docker-compose.prod.yml` | 14 services (prod, nuxt-web removed) |
| `docker/scripts/e2e-smoke-test.sh` | 22-check integration smoke test |
| `docker/scripts/seed-sample-data.sh` | Demo data for all 7 services |
| `frontend/report-system-angular/.../src/environments/` | Angular env config (dev + prod) |
| `frontend/report-system-angular/.../app/core/` | Core services, interceptors, models |
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
- `core/` — interceptors (auth, branch), services (branch, keycloak-init), models (User)
- `shared/` — layout (MainLayout, Sidebar, Topbar)
- `features/` — 7 lazy-loaded modules, all fully built:

  | Module | Components |
  |--------|-----------|
  | Auth | LoginComponent, authGuard |
  | Dashboard | DashboardComponent (4 stat cards) |
  | Property | PropertyList, PropertyForm, PropertyDetail, UnitList, UnitFormDialog, LeaseList, LeaseFormDialog |
  | Restaurant | OutletList, OutletDetail, MenuList, OrderList, CustomerList, ReservationList |
  | Inventory | ProductList, ProductForm, SupplierList, PurchaseOrderList, StockTransferList |
  | Finance | ChartOfAccountsList, AccountForm, JournalEntryList, InvoiceList, InvoiceDetail, InvoiceForm, TaxList, EmployeeList, PayrollList |
  | Payment | PaymentList |
  | Reporting | ReportList (dashboard summary cards + report table) |

**Key config**: PrimeNG Aura theme with dark mode via `.dark` class, Tailwind for utility CSS, Keycloak SSO with `check-sso` init, HTTP interceptors for token + tenant/branch headers.

**Docker**: `docker-compose.yml` now includes `report-web` service (nginx, port 4200). Build with `docker compose -f docker/docker-compose.yml up -d --build report-web`.

Backend services also testable via `docker/scripts/e2e-smoke-test.sh` or direct API calls through gateway at `http://localhost:8080`.

## Default Credentials

- Email: `admin@demo.com` / Password: `Demo123!`
- Tenant: `demo-corp` (UUID `00000000-0000-0000-0000-000000000001`)
- HQ Branch UUID: `00000000-0000-0000-0000-000000000010`
- API: `POST http://localhost:8080/api/auth/login`
