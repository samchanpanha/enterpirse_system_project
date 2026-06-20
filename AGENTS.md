# AGENTS.md — Report_System

## Architecture

7 Spring Boot 3.2.5 microservices + Eureka + Gateway + Kafka + PostgreSQL-per-service. Nuxt.js 3 frontend. All services use **hexagonal (ports & adapters)** architecture.

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
- Frontend: `branchStore.$apiWithBranch()` auto-injects `?branchId=X` on every call

## Commands

```bash
# Backend
mvn clean compile                          # Compile all 15 modules (Java 21, Maven 3.9)
mvn test -q                                # Run all tests (48 total: auth 27 + property 21)
mvn test -pl services/property-service -q  # Single service tests
mvn package -DskipTests                    # Build JARs

# Frontend (from frontend/report-system-web/)
npm run typecheck                          # nuxt typecheck (0 errors expected)
npm run lint                               # eslint (0 errors, debug console.warn OK)
npm run build                              # Nuxt build -> .output/ (~4 MB)
npm run dev                                # Dev server on :3000, proxies /api -> :8080

# Docker (from project root, NOT docker/)
docker compose -f docker/docker-compose.yml up -d       # Must use --project-directory .
docker/scripts/seed-sample-data.sh                      # Seed demo data
docker/scripts/e2e-smoke-test.sh                        # 22 integration checks
```

## Known Gotchas

### Frontend
- **`$apiWithBranch()` paths MUST NOT include `/api` prefix** — the `branch.ts` helper prepends it automatically. Wrong: `$apiWithBranch('/api/payment/...')`. Correct: `$apiWithBranch('/payment/...')`.
- **`fetchBranches()` needs `Authorization` header** (it uses plain `$fetch`, not `$apiWithBranch`)
- **SSO bridge URLs need `/api` prefix**: `${apiBaseUrl}/api/auth/sso-login`, NOT `${apiBaseUrl}/auth/sso-login`
- **`nuxt.config.ts`** has `srcDir: 'app/'`, so imports use `~/stores/X`, `~/shared/types/X`, `~/components/admin/X` (NOT `~/app/...`)
- **`tailwindcss.configPath`** is `'./tailwind.config.ts'` (relative to project root, NOT `'../'`)
- **Auto-imports**: Nuxt auto-imports composables from `app/composables/` and components from `app/components/` (`pathPrefix: false` → flat names like `<AdminTable />`)
- **Dark mode**: CSS class `dark` on `<html>`, toggled via `useLayoutState().toggleDarkMode()`

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
| `docker/docker-compose.yml` | 18 services (dev) |
| `docker/docker-compose.prod.yml` | 15 services (prod, with health checks) |
| `docker/scripts/e2e-smoke-test.sh` | 22-check integration smoke test |
| `docker/scripts/seed-sample-data.sh` | Demo data for all 7 services |
| `frontend/report-system-web/nuxt.config.ts` | Nuxt config with proxy, runtimeConfig, tailwind |
| `frontend/report-system-web/app/stores/branch.ts` | Branch store with `$apiWithBranch()` helper |

## Default Credentials

- Email: `admin@demo.com` / Password: `Demo123!`
- Tenant: `demo-corp` (UUID `00000000-0000-0000-0000-000000000001`)
- HQ Branch UUID: `00000000-0000-0000-0000-000000000010`
- Login: `http://localhost:3000/login`
- API: `POST http://localhost:8080/api/auth/login`
