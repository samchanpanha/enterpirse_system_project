# Report System

> Multi-tenant SaaS platform for property, restaurant, and inventory management with Keycloak SSO and inter-branch transfers.
> Built as 7 Spring Boot microservices + Nuxt.js 3 frontend + Keycloak identity provider, all containerized with Docker Compose.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![Tests](https://img.shields.io/badge/tests-27%20passing-brightgreen)](#docker-scripts)
[![Sprint](https://img.shields.io/badge/sprint-12-blue)](#roadmap)

---

## Table of contents

- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Quick start](#quick-start)
- [Services](#services)
- [Frontend](#frontend)
- [Testing](#testing)
- [Deployment](#deployment)
- [Multi-branch & SSO](#multi-branch--sso)
- [Roadmap](#roadmap)
- [Project structure](#project-structure)
- [Contributing](#contributing)

---

## Architecture

```
              ┌──────────────────────────────────────────┐
              │       Nuxt.js 3 Frontend (:3000)         │
              │   keycloak-js + Pinia + branch selector  │
              └────────────────┬─────────────────────────┘
                               │ OIDC / Bearer
                               ▼
   ┌──────────────────────────────────────────────────────┐
   │      Spring Cloud Gateway (:8080)                     │
   │  • OIDC token validation (Keycloak JWKS)            │
   │  • X-Tenant-Id, X-Branch-Id, X-User-Id propagation   │
   │  • BranchContextFilter (branch-level authorization)   │
   └──┬──┬──┬──┬──┬──┬──┬──────────────────────────────┘
      ▼  ▼  ▼  ▼  ▼  ▼  ▼
    auth property restaurant inventory finance payment reporting
      │   │        │         │         │       │        │
      ▼   ▼        ▼         ▼         ▼       ▼        ▼
    7 × PostgreSQL DBs (one per service, with `branches` table each)
      │   │        │         │         │       │        │
      └─┬─┴────────┴─────────┴─────────┴───────┴────────┘
        ▼
   ┌────────┐  ┌────────┐  ┌────────┐
   │Keycloak│  │ Eureka │  │  Kafka  │
   │  :8180 │  │  :8761 │  │  :9092  │
   └────────┘  └────────┘  └────────┘
```

Each service:
- Owns its own database (per-service DBs avoid coupling)
- Speaks only REST to other services (no DB joins across services)
- Publishes/subscribes domain events via Kafka
- Registers itself with Eureka for service discovery
- Reads tenant + branch context from HTTP headers (set by the gateway)

## Tech stack

| Layer        | Technology                                              |
| ------------ | ------------------------------------------------------- |
| Frontend     | Nuxt.js 3, Vue 3, Pinia, TypeScript, Tailwind CSS      |
| API Gateway  | Spring Cloud Gateway (reactive WebFlux)                |
| Identity     | Keycloak 24 (OIDC, realm-per-tenant)                   |
| Microservices| Spring Boot 3.2, Java 21, Hexagonal architecture       |
| Databases    | PostgreSQL 16 (one per service, Flyway migrations)     |
| Service disc.| Netflix Eureka                                         |
| Messaging    | Apache Kafka (Confluent 7.5)                           |
| Caching      | Redis 7                                                 |
| Build        | Maven 3.9, Docker 29, Docker Compose                   |
| Tests        | JUnit 5, Mockito, Testcontainers, bash + curl E2E     |

## Quick start

### Prerequisites

- Docker 24+ with Docker Compose v2
- Java 21 (for local Maven builds)
- Node 20+ (for frontend local dev)
- 8 GB RAM, 20 GB disk

### One-command launch

```bash
# 1. Clone and enter the repo
git clone <repo> && cd Report_System

# 2. Bring up the entire stack (Postgres, Keycloak, Kafka, Eureka, Gateway, 7 services, Nuxt)
docker compose -f docker/docker-compose.yml up -d

# 3. Wait for everything to be healthy (~3 minutes the first time)
./docker/scripts/wait-healthy.sh

# 4. Seed sample data + run E2E test
./docker/scripts/seed-sample-data.sh
./docker/scripts/e2e-smoke-test.sh
```

Open <http://localhost:3000> for the UI, or <http://localhost:8761> for Eureka.

### Default credentials

| Field    | Value                                              |
| -------- | -------------------------------------------------- |
| Email    | `admin@demo.com`                                   |
| Password | `Demo123!`                                         |
| Realm    | `demo-corp`                                        |
| Tenant   | `00000000-0000-0000-0000-000000000001`              |

Login via either:
- **UI**: <http://localhost:3000/login> → "Sign in with Keycloak (SSO)"
- **API**: `POST http://localhost:8180/realms/demo-corp/protocol/openid-connect/token` (client_credentials / password grant)

## Services

| Port | Service             | Description                                  |
| ---- | ------------------- | -------------------------------------------- |
| 8180 | Keycloak            | OIDC provider, realm-per-tenant              |
| 8080 | Gateway             | API gateway + OIDC validation + branch filter|
| 8761 | Eureka              | Service discovery                            |
| 8081 | auth-service        | Tenants, branches, users, JWT, SSO bridge    |
| 8082 | property-service    | Properties, units, leases, schedules         |
| 8083 | restaurant-service  | Outlets, tables, menu, orders, KDS          |
| 8084 | inventory-service   | Products, warehouses, stock, transfers      |
| 8085 | finance-service     | Chart of accounts, journal entries, tax      |
| 8086 | payment-service     | ABA/Wing/Pi Pay/Cash gateways, reconciliation|
| 8087 | reporting-service   | Report definitions, dashboards, snapshots   |
| 3000 | nuxt-web            | Nuxt 3 frontend                               |

See [`plan.md`](plan.md) for the full architecture plan, [`AGENTS.md`](AGENTS.md) for implementation status.

## Frontend

The Nuxt 3 frontend at `frontend/report-system-web/`:

- `app/pages/auth/` — Login, register, Keycloak callback
- `app/pages/app/` — Authenticated app:
  - `dashboard.vue` — overview metrics
  - `property/` — properties, units, leases, schedules, maintenance
  - `restaurant/` — POS, menu, orders, KDS, reservations, customers
  - `inventory/` — products, suppliers, purchase orders, **stock transfers**
  - `finance/` — dashboard, accounts, invoices, tax, employees, payroll
  - `payment/` — transactions, reconciliation
  - `reporting/` — reports, dashboards
  - `admin/` — branch management
- `app/stores/` — Pinia stores (auth, branch, property, restaurant, inventory, finance, payment, reporting)
- `app/composables/useAuth.ts` — login + Keycloak bridge
- `app/components/BranchSelector.vue` — branch context switcher in the header

## Testing

### Unit tests (Maven / JUnit 5)

```bash
mvn test -pl services/auth-service,shared/security-core
```

Current coverage:
- `AuthServiceTest` (11 tests) — register, login, JWT generation, refresh
- `BranchServiceTest` (10 tests) — CRUD + default branch logic
- `UserBranchServiceTest` (6 tests) — assign, unassign, idempotency
- `JwtTokenProviderTest` (5 tests) — generate, validate, expiry, wrong secret

**27 tests, 0 failures.**

### End-to-end smoke test (bash + curl)

```bash
./docker/scripts/e2e-smoke-test.sh
```

Verifies the **entire** stack:
1. Keycloak OIDC login + JWT claims (tenantId, defaultBranchId)
2. SSO bridge to legacy JWT
3. All 7 services reachable through the gateway
4. Branch CRUD
5. User-branch auto-assignment
6. BranchContextFilter (allowed vs disallowed branch)
7. Full stock transfer flow (DRAFT → SHIPPED → RECEIVED)
8. Kafka event consumed by finance-service
9. Inter-branch journal entry (4 balanced lines, IBC account)

**22 checks, all green.**

## Deployment

### Local (Docker Compose)

```bash
docker compose -f docker/docker-compose.yml up -d
```

### Stop / restart

```bash
docker compose -f docker/docker-compose.yml stop
docker compose -f docker/docker-compose.yml restart inventory-service
```

### Production (Kubernetes / Kustomize)

See [`k8s/`](k8s/) for the Kustomize manifests and [`k8s/README.md`](k8s/README.md) for the deployment guide.

The compose file maps 1:1 to the K8s manifests: 18 services (7 app + gateway + eureka + keycloak + 2 postgres + kafka + zookeeper + redis + minio + zipkin + nuxt-web).

### Migrate users from auth-service to Keycloak

```bash
./docker/scripts/migrate-users-to-keycloak.sh --dry-run    # preview
./docker/scripts/migrate-users-to-keycloak.sh             # run
```

One-time script. Creates a Keycloak realm per tenant + Keycloak user per auth-service user, with the `tenantId` and `defaultBranchId` attributes. Users get a temporary password and must reset on first login.

## Multi-branch & SSO

The system supports **multi-tenant** and **multi-branch** out of the box:

- **Multi-tenant**: every domain entity has a `tenant_id` column. Keycloak uses one realm per tenant.
- **Multi-branch**: every domain entity also has a `branch_id` column. Branches are seeded with an HQ default per tenant.
- **Branch context**: the gateway sets `X-Branch-Id` on every request. Services can either scope queries to that branch (`?branchId=X`) or use a default.
- **BranchContextFilter**: the gateway enforces that the requesting user is assigned to the requested branch (403 otherwise).
- **Inter-branch transfers**: stock can be moved between branches via `/api/inventory/transfers`. The flow:
  ```
  DRAFT → SHIPPED (decrements source stock)
        → RECEIVED (increments target stock + posts 4-line inter-branch journal entry)
        → CANCELLED (restocks if SHIPPED)
  ```
- **Inter-branch accounting**: 4-line journal entry via Kafka consumer:
  ```
  DR  Inventory (target)  ←  CR  Inter-Branch Clearing
  DR  Inter-Branch Clearing  ←  CR  Inventory (source)
  ```
  The two IBC lines net to zero, each branch's books balance.

## Roadmap

| Sprint | Status   | Deliverable                                                     |
| ------ | -------- | --------------------------------------------------------------- |
| 1      | ✅       | Architecture plan, parent POM, Eureka, Gateway, Auth skeleton   |
| 2      | ✅       | Property + Restaurant services (hexagonal)                      |
| 3      | ✅       | Inventory + Finance services (hexagonal)                        |
| 4      | ✅       | Payment + Reporting services (hexagonal)                        |
| 5      | ✅       | Squirrel audit + 12 bug fixes                                   |
| 6      | ✅       | Local deploy + sample data + login fix                          |
| 7      | ✅       | **Keycloak SSO** + **multi-branch foundation**                  |
| 8      | ✅       | BranchContextFilter + user-branch-role mapping                 |
| 9      | ✅       | **Cross-branch stock transfers** + inter-branch journals        |
| 10     | ✅       | **E2E tests** + migration tools + docs                          |
| 11     | ⏳       | Cross-branch reports + bulk operations                          |

## Project structure

```
Report_System/
├── pom.xml                         # Parent POM (12 modules)
├── plan.md                         # 1668-line architecture plan
├── AGENTS.md                       # Implementation status
├── README.md                       # This file
├── shared/                         # Shared modules
│   ├── common-dto/                 # Cross-service DTOs + events
│   ├── security-core/              # JWT provider
│   ├── tenant-context/             # Tenant + branch context
│   └── tax-engine/                 # Cambodia tax calculators
├── infrastructure/
│   ├── eureka/                     # Service discovery
│   └── gateway/                    # API gateway (OIDC, branch filter)
├── services/                       # 7 microservices
│   ├── auth-service/               # Tenants, branches, users, JWT
│   ├── property-service/           # Properties, units, leases
│   ├── restaurant-service/         # Outlets, menu, orders
│   ├── inventory-service/          # Products, stock, transfers
│   ├── finance-service/            # Chart of accounts, journal entries
│   ├── payment-service/            # ABA/Wing/Pi Pay/Cash
│   └── reporting-service/          # Report definitions, dashboards
├── frontend/report-system-web/      # Nuxt 3 app
├── docker/
│   ├── docker-compose.yml          # All 18 services
│   ├── keycloak/realm/             # Realm export
│   └── scripts/                    # Seed, deploy, E2E, migration
└── k8s/                            # Kustomize manifests
```

## Contributing

1. Read [`AGENTS.md`](AGENTS.md) for current status
2. Check [`plan.md`](plan.md) for architecture
3. Run `./docker/scripts/e2e-smoke-test.sh` before pushing
4. Add unit tests for new business logic (`mvn test -pl <service>`)
5. Follow the hexagonal architecture: domain → port → service → entity → adapter → controller
6. Use `branchStore.$apiWithBranch()` in frontend (not direct `$api`)

## License

MIT
