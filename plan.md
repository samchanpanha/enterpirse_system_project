# Plan — Report_System MVP (Microservice Architecture)

## Overview

Rental Housing & Real Estate SaaS MVP targeting the Cambodian market. Dual vertical: real estate (property management / schedules) and restaurant (POS / stock / suppliers / CRM). Shared engine for accounting, HR, payroll, and Cambodia tax compliance. Backend: Spring Boot 3.x microservices. Frontend: Nuxt.js 3. DevOps: Docker Compose (dev/prod) with documented K8s migration path.

---

## 1. Architecture

> **Note (2026-06-18):** This diagram reflects the **current state (post-Sprint 4 + audit fixes)**. For the planned **Keycloak SSO + Multi-Branch** architecture (Sprints 9–11), see [Section 14](#14-keycloak-sso--multi-branch-architecture-sprints-911).

```
┌──────────────────────────────────────────────────────────────┐
│                   Nuxt.js 3 Frontend (SSR)                    │
│   Layers: core | property | restaurant | finance              │
│   Tailwind CSS | Pinia | TypeScript | Chart.js                │
└─────────────────────────┬────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────┐
│              Spring Cloud Gateway (port 8080)                  │
│  Routes: /api/auth/* → auth-service:8081                       │
│          /api/property/* → property-service:8082               │
│          /api/restaurant/* → restaurant-service:8083           │
│          /api/inventory/* → inventory-service:8084            │
│          /api/finance/* → finance-service:8085                │
│          /api/payment/* → payment-service:8086                │
│          /api/reporting/* → reporting-service:8087            │
│  Filters: JWT validation, tenant context, rate limit, logging  │
└──┬───┬───┬───┬───┬───┬───┬───────────────────────────────────┘
   │   │   │   │   │   │   │
   ▼   ▼   ▼   ▼   ▼   ▼   ▼
┌────┐┌────┐┌────┐┌────┐┌────┐┌────┐┌────────┐
│Auth││Prop││Rest││Inv ││Fin ││Pay ││Report  │
│    ││    ││    ││    ││    ││    ││        │
│ JWT││Uni ││POS ││Stoc││GL  ││ABA ││Dashboa │
│Ten ││Sched││KDS ││Supp││Tax ││Wing││PDF     │
│Role││Main││CRM ││POs ││HR  ││Pi  ││Export  │
│    ││tena││    ││    ││Payr││Cash││        │
│    ││nts ││    ││    ││oll ││    ││        │
└─┬──┘└─┬──┘└─┬──┘└─┬──┘└─┬──┘└─┬──┘└───┬────┘
  │     │     │     │     │     │        │
  └─────┴─────┴─────┴─────┴─────┴────────┘
                    │
              ┌─────▼──────┐         ┌──────────────┐
              │  Eureka    │         │  Kafka       │
              │  Discovery │         │  Event Bus   │
              │  :8761     │         │  :9092       │
              └────────────┘         └──────┬───────┘
                    │                       │
              ┌─────▼───────────────────────▼──────────────┐
              │  PostgreSQL (7 databases, 1 cluster)        │
              │  auth_db | property_db | restaurant_db      │
              │  inventory_db | finance_db | payment_db     │
              │  reporting_db                               │
              ├────────────────────────────────────────────┤
              │  Redis  |  MinIO  |  Zipkin                │
              └────────────────────────────────────────────┘
```

### Planned (Sprint 9–11)

Keycloak SSO replaces auth-service JWT. Branches added as first-class entity. See [Section 14](#14-keycloak-sso--multi-branch-architecture-sprints-911) for full architecture.

---

## 2. Microservices Breakdown

### Service Catalog

| Service | Port | DB | Responsibility |
|---------|------|-----|---------------|
| **Auth Service** | 8081 | `auth_db` | JWT auth, refresh tokens, multi-tenant registry, RBAC |
| **Property Service** | 8082 | `property_db` | Properties, units, schedule engine (hourly→yearly), tenant lifecycle, maintenance tickets |
| **Restaurant Service** | 8083 | `restaurant_db` | POS terminal, menu, KDS, table/floor management, QR ordering, customer CRM, loyalty |
| **Inventory Service** | 8084 | `inventory_db` | Stock-in/out, batch/FEFO tracking, supplier registry, purchase orders, threshold alerts |
| **Finance Service** | 8085 | `finance_db` | GL, AR/AP, journal entries, Cambodia tax suite, payroll, NSSF |
| **Payment Service** | 8086 | `payment_db` | ABA PayWay, Wing, Pi Pay, cash — transaction routing, refunds, reconciliation |
| **Reporting Service** | 8087 | `reporting_db` | Dashboards, PDF/CSV/XML exports, consolidated P&L, GDT e-filing, occupancy analytics |

### Shared Infrastructure

| Component | Tech | Purpose |
|-----------|------|---------|
| API Gateway | Spring Cloud Gateway | Entry point, routing, JWT validation, CORS, rate limiting |
| Service Discovery | Netflix Eureka | Dynamic registration & load-balanced lookups |
| Event Bus | Apache Kafka | Async cross-domain events (sale→inventory→accounting) |
| Config | Env vars / Spring Cloud Config | Centralized external config per service |
| Circuit Breaker | Resilience4j | Fault tolerance between services |
| Tracing | Micrometer + Zipkin | Distributed request tracing |
| Monitoring | Prometheus + Grafana | Metrics per service |
| Caching | Redis 7 | Session store, rate limiter backends, cache hot data |

---

## 3. Event-Driven Communication (Kafka)

### Event Catalog

| Topic | Publisher | Consumers | Payload |
|-------|-----------|-----------|---------|
| `sale.completed` | Restaurant Service | Inventory, Finance, Reporting | `{saleId, items[], totals, tax, paymentMethod}` |
| `rent.invoice.generated` | Property Service | Finance, Payment, Reporting | `{invoiceId, tenantId, unitId, amount, dueDate}` |
| `payment.received` | Payment Service | Finance, Property, Restaurant, Reporting | `{paymentId, invoiceId, amount, method, status}` |
| `payment.failed` | Payment Service | Finance, Property, Reporting | `{paymentId, invoiceId, reason}` |
| `stock.received` | Inventory Service | Finance, Reporting | `{poId, items[], totalCost, receivedDate}` |
| `stock.low` | Inventory Service | Restaurant, Property | `{itemId, currentQty, threshold}` |
| `payroll.processed` | Finance Service | Reporting | `{periodStart, periodEnd, totalGross, totalTax, totalNSSF}` |
| `tenant.moved-in` | Property Service | Finance, Reporting | `{tenantId, unitId, leaseStart, rent}` |
| `tenant.moved-out` | Property Service | Finance, Reporting | `{tenantId, unitId, leaseEnd, outstanding}` |

### Event flow example (POS sale):

```
Restaurant Service                  Inventory Service           Finance Service
     │                                    │                          │
     │─── sale.completed ───────────────►│                          │
     │                                    │── deduct stock ──┐     │
     │                                    │◄─ complete ──────┘     │
     │                                                            │
     │──────────────────── sale.completed ──────────────────────►│
     │                                                            │
     │                                    ┌────────────────────────┘
     │                                    ▼
     │                            Post journal entry:
     │                            DR Cash/AR
     │                            CR Revenue
     │                            CR VAT Payable
```

---

## 4. Database Schemas (Per Service)

### 4.1 Auth Service — `auth_db`

```sql
tenants (
  id              UUID PK,
  name            VARCHAR(255) NOT NULL,
  slug            VARCHAR(100) UNIQUE NOT NULL,
  domain          VARCHAR(255),
  logo_url        TEXT,
  is_active       BOOLEAN DEFAULT true,
  subscription    VARCHAR(50),              -- starter, business, enterprise
  settings        JSONB DEFAULT '{}',
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

users (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  email           VARCHAR(255) NOT NULL,
  password_hash   VARCHAR(255) NOT NULL,
  first_name      VARCHAR(100),
  last_name       VARCHAR(100),
  phone           VARCHAR(50),
  locale          VARCHAR(10) DEFAULT 'km',
  is_active       BOOLEAN DEFAULT true,
  last_login_at   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ,
  UNIQUE (tenant_id, email)
);

roles (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(100) NOT NULL,
  description     TEXT,
  is_system       BOOLEAN DEFAULT false,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (tenant_id, name)
);

permissions (
  id              UUID PK,
  code            VARCHAR(100) UNIQUE NOT NULL,
  name            VARCHAR(255),
  module          VARCHAR(50),
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

role_permissions (
  role_id         UUID FK NOT NULL,
  permission_id   UUID FK NOT NULL,
  PRIMARY KEY (role_id, permission_id)
);

user_roles (
  user_id         UUID FK NOT NULL,
  role_id         UUID FK NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

refresh_tokens (
  id              UUID PK,
  user_id         UUID FK NOT NULL,
  token_hash      VARCHAR(255) NOT NULL,
  expires_at      TIMESTAMPTZ NOT NULL,
  revoked         BOOLEAN DEFAULT false,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);
CREATE INDEX idx_roles_tenant ON roles(tenant_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
```

### 4.2 Property Service — `property_db`

```sql
properties (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  type            VARCHAR(50),                -- apartment, house, office, shop, land, warehouse
  address         TEXT,
  city            VARCHAR(100),
  district        VARCHAR(100),
  lat             DECIMAL(10,7),
  lng             DECIMAL(10,7),
  total_units     INTEGER DEFAULT 0,
  status          VARCHAR(50) DEFAULT 'active',
  owner_name      VARCHAR(255),
  owner_phone     VARCHAR(50),
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

units (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  property_id     UUID FK NOT NULL,
  label           VARCHAR(100) NOT NULL,
  floor           INTEGER,
  bedrooms        INTEGER DEFAULT 1,
  bathrooms       INTEGER DEFAULT 1,
  area_sqm        DECIMAL(10,2),
  rent_amount     DECIMAL(12,2),
  deposit_amount  DECIMAL(12,2),
  currency        VARCHAR(3) DEFAULT 'USD',
  status          VARCHAR(50) DEFAULT 'vacant',
  type            VARCHAR(50),
  amenities       JSONB DEFAULT '[]',
  images          JSONB DEFAULT '[]',
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

leases (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  unit_id         UUID FK NOT NULL,
  tenant_name     VARCHAR(255) NOT NULL,
  tenant_phone    VARCHAR(50),
  tenant_email    VARCHAR(255),
  id_type         VARCHAR(50),
  id_number       VARCHAR(100),
  start_date      DATE NOT NULL,
  end_date        DATE,
  rent_amount     DECIMAL(12,2) NOT NULL,
  deposit_amount  DECIMAL(12,2),
  payment_day     INTEGER DEFAULT 1,
  status          VARCHAR(50) DEFAULT 'active',
  documents       JSONB DEFAULT '[]',
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

schedules (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  unit_id         UUID FK NOT NULL,
  title           VARCHAR(255) NOT NULL,
  description     TEXT,
  type            VARCHAR(50) NOT NULL,
  interval_type   VARCHAR(50) NOT NULL,
  start_time      TIMESTAMPTZ NOT NULL,
  end_time        TIMESTAMPTZ,
  recurring_rule  JSONB,
  status          VARCHAR(50) DEFAULT 'scheduled',
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

maintenance_tickets (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  unit_id         UUID FK NOT NULL,
  reported_by     VARCHAR(255),
  title           VARCHAR(255) NOT NULL,
  description     TEXT,
  priority        VARCHAR(20) DEFAULT 'medium',
  category        VARCHAR(50),
  status          VARCHAR(50) DEFAULT 'open',
  assigned_to     VARCHAR(255),
  cost_estimate   DECIMAL(12,2),
  actual_cost     DECIMAL(12,2),
  completed_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_units_property ON units(property_id);
CREATE INDEX idx_leases_unit_status ON leases(unit_id, status);
CREATE INDEX idx_leases_end_date ON leases(end_date);
CREATE INDEX idx_schedules_unit_time ON schedules(unit_id, start_time);
CREATE INDEX idx_schedules_interval ON schedules(interval_type);
CREATE INDEX idx_maintenance_unit ON maintenance_tickets(unit_id);
CREATE INDEX idx_maintenance_status ON maintenance_tickets(status);
```

### 4.3 Restaurant Service — `restaurant_db`

```sql
outlets (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  address         TEXT,
  phone           VARCHAR(50),
  email           VARCHAR(255),
  tax_number      VARCHAR(100),
  type            VARCHAR(50) DEFAULT 'restaurant',
  currency        VARCHAR(3) DEFAULT 'USD',
  settings        JSONB DEFAULT '{}',
  is_active       BOOLEAN DEFAULT true,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

tables (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  outlet_id       UUID FK NOT NULL,
  label           VARCHAR(50) NOT NULL,
  capacity        INTEGER DEFAULT 4,
  floor           VARCHAR(50),
  section         VARCHAR(100),
  pos_x           DECIMAL(8,2),
  pos_y           DECIMAL(8,2),
  status          VARCHAR(20) DEFAULT 'available',
  qr_code_url     TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

categories (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  outlet_id       UUID FK,
  name            VARCHAR(255) NOT NULL,
  description     TEXT,
  sort_order      INTEGER DEFAULT 0,
  is_active       BOOLEAN DEFAULT true,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

menu_items (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  category_id     UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  name_kh         VARCHAR(255),
  description     TEXT,
  description_kh  TEXT,
  price           DECIMAL(12,2) NOT NULL,
  currency        VARCHAR(3) DEFAULT 'USD',
  tax_rate        DECIMAL(5,2) DEFAULT 10.00,
  image_url       TEXT,
  options         JSONB DEFAULT '[]',
  modifiers       JSONB DEFAULT '[]',
  is_active       BOOLEAN DEFAULT true,
  sort_order      INTEGER DEFAULT 0,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

customers (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  outlet_id       UUID FK,
  name            VARCHAR(255) NOT NULL,
  phone           VARCHAR(50),
  email           VARCHAR(255),
  birthday        DATE,
  is_vip          BOOLEAN DEFAULT false,
  notes           TEXT,
  total_visits    INTEGER DEFAULT 0,
  total_spent     DECIMAL(14,2) DEFAULT 0,
  last_visit_at   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

orders (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  outlet_id       UUID FK NOT NULL,
  table_id        UUID FK,
  customer_id     UUID FK,
  order_number    VARCHAR(50) NOT NULL,
  type            VARCHAR(20) DEFAULT 'dine_in',
  status          VARCHAR(50) DEFAULT 'pending',
  subtotal        DECIMAL(12,2) NOT NULL,
  discount        DECIMAL(12,2) DEFAULT 0,
  tax_amount      DECIMAL(12,2) DEFAULT 0,
  service_charge  DECIMAL(12,2) DEFAULT 0,
  total           DECIMAL(12,2) NOT NULL,
  payment_status  VARCHAR(20) DEFAULT 'unpaid',
  notes           TEXT,
  served_by       UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  completed_at    TIMESTAMPTZ
);

order_items (
  id              UUID PK,
  order_id        UUID FK NOT NULL,
  menu_item_id    UUID FK NOT NULL,
  quantity        INTEGER NOT NULL DEFAULT 1,
  unit_price      DECIMAL(12,2) NOT NULL,
  modifiers       JSONB DEFAULT '[]',
  subtotal        DECIMAL(12,2) NOT NULL,
  notes           TEXT,
  status          VARCHAR(20) DEFAULT 'pending',
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

reservations (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  outlet_id       UUID FK NOT NULL,
  table_id        UUID FK,
  customer_id     UUID FK,
  guest_name      VARCHAR(255),
  guest_phone     VARCHAR(50),
  guest_count     INTEGER NOT NULL,
  reservation_time TIMESTAMPTZ NOT NULL,
  duration_minutes INTEGER DEFAULT 120,
  status          VARCHAR(20) DEFAULT 'confirmed',
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_outlets_tenant ON outlets(tenant_id);
CREATE INDEX idx_tables_outlet ON tables(outlet_id);
CREATE INDEX idx_tables_status ON tables(status);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_active ON menu_items(is_active);
CREATE INDEX idx_orders_outlet_created ON orders(outlet_id, created_at);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_reservations_time ON reservations(outlet_id, reservation_time);
```

### 4.4 Inventory Service — `inventory_db`

```sql
suppliers (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  contact_person  VARCHAR(255),
  phone           VARCHAR(50),
  email           VARCHAR(255),
  address         TEXT,
  tax_number      VARCHAR(100),
  payment_terms   VARCHAR(100),
  currency        VARCHAR(3) DEFAULT 'USD',
  is_active       BOOLEAN DEFAULT true,
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

warehouses (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  type            VARCHAR(50) DEFAULT 'main',
  location        TEXT,
  is_active       BOOLEAN DEFAULT true,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

product_categories (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  parent_id       UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

products (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  category_id     UUID FK,
  name            VARCHAR(255) NOT NULL,
  name_kh         VARCHAR(255),
  sku             VARCHAR(100),
  barcode         VARCHAR(100),
  unit            VARCHAR(20) DEFAULT 'pcs',
  unit_price      DECIMAL(12,2),
  cost_price      DECIMAL(12,2),
  min_stock       DECIMAL(12,2) DEFAULT 0,
  max_stock       DECIMAL(12,2),
  is_tracked      BOOLEAN DEFAULT true,
  is_active       BOOLEAN DEFAULT true,
  image_url       TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

stock_entries (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  warehouse_id    UUID FK NOT NULL,
  product_id      UUID FK NOT NULL,
  quantity        DECIMAL(12,2) NOT NULL,
  unit_cost       DECIMAL(12,2),
  batch_number    VARCHAR(100),
  expiry_date     DATE,
  reference_type  VARCHAR(50),
  reference_id    UUID,
  notes           TEXT,
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

stock_exits (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  warehouse_id    UUID FK NOT NULL,
  product_id      UUID FK NOT NULL,
  quantity        DECIMAL(12,2) NOT NULL,
  reference_type  VARCHAR(50),
  reference_id    UUID,
  notes           TEXT,
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

purchase_orders (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  supplier_id     UUID FK NOT NULL,
  po_number       VARCHAR(50) NOT NULL,
  status          VARCHAR(20) DEFAULT 'draft',
  order_date      DATE NOT NULL,
  expected_date   DATE,
  received_date   DATE,
  subtotal        DECIMAL(12,2),
  tax_amount      DECIMAL(12,2),
  shipping_cost   DECIMAL(12,2),
  total           DECIMAL(12,2),
  currency        VARCHAR(3) DEFAULT 'USD',
  notes           TEXT,
  created_by      UUID FK,
  approved_by     UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

purchase_order_items (
  id                UUID PK,
  po_id             UUID FK NOT NULL,
  product_id        UUID FK NOT NULL,
  quantity_ordered  DECIMAL(12,2) NOT NULL,
  quantity_received DECIMAL(12,2) DEFAULT 0,
  unit_cost         DECIMAL(12,2) NOT NULL,
  subtotal          DECIMAL(12,2) NOT NULL,
  created_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_stock_entries_product ON stock_entries(product_id, created_at);
CREATE INDEX idx_stock_entries_warehouse ON stock_entries(warehouse_id);
CREATE INDEX idx_stock_exits_product ON stock_exits(product_id, created_at);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_po_items_po ON purchase_order_items(po_id);
```

### 4.5 Finance Service — `finance_db`

```sql
chart_of_accounts (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  code            VARCHAR(20) UNIQUE NOT NULL,
  name            VARCHAR(255) NOT NULL,
  type            VARCHAR(50) NOT NULL,
  is_active       BOOLEAN DEFAULT true,
  is_contra       BOOLEAN DEFAULT false,
  parent_id       UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

journal_entries (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  entry_number    VARCHAR(50) NOT NULL,
  entry_date      DATE NOT NULL,
  description     TEXT,
  reference_type  VARCHAR(50),
  reference_id    UUID,
  is_posted       BOOLEAN DEFAULT false,
  posted_at       TIMESTAMPTZ,
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

journal_entry_lines (
  id                UUID PK,
  journal_entry_id  UUID FK NOT NULL,
  account_id        UUID FK NOT NULL,
  debit             DECIMAL(14,2) DEFAULT 0,
  credit            DECIMAL(14,2) DEFAULT 0,
  description       TEXT,
  created_at        TIMESTAMPTZ DEFAULT NOW()
);

invoices (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  invoice_number  VARCHAR(50) UNIQUE NOT NULL,
  invoice_type    VARCHAR(20) NOT NULL,
  source_type     VARCHAR(50),
  source_id       UUID,
  customer_name   VARCHAR(255),
  customer_tin    VARCHAR(100),
  issue_date      DATE NOT NULL,
  due_date        DATE NOT NULL,
  subtotal        DECIMAL(14,2) NOT NULL,
  discount        DECIMAL(14,2) DEFAULT 0,
  tax_amount      DECIMAL(14,2) DEFAULT 0,
  total           DECIMAL(14,2) NOT NULL,
  amount_paid     DECIMAL(14,2) DEFAULT 0,
  balance_due     DECIMAL(14,2) NOT NULL,
  status          VARCHAR(20) DEFAULT 'pending',
  currency        VARCHAR(3) DEFAULT 'USD',
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

invoice_items (
  id              UUID PK,
  invoice_id      UUID FK NOT NULL,
  description     TEXT NOT NULL,
  quantity        DECIMAL(12,2) DEFAULT 1,
  unit_price      DECIMAL(14,2) NOT NULL,
  tax_rate        DECIMAL(5,2) DEFAULT 10.00,
  tax_amount      DECIMAL(14,2) DEFAULT 0,
  total           DECIMAL(14,2) NOT NULL,
  account_id      UUID FK
);

tax_records (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  tax_type        VARCHAR(50) NOT NULL,
  period_month    INTEGER NOT NULL,
  period_year     INTEGER NOT NULL,
  taxable_amount  DECIMAL(14,2) NOT NULL,
  tax_rate        DECIMAL(5,2) NOT NULL,
  tax_amount      DECIMAL(14,2) NOT NULL,
  source_type     VARCHAR(50),
  source_id       UUID,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (tenant_id, tax_type, period_month, period_year, source_id)
);

tax_filing_reports (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  tax_type        VARCHAR(50) NOT NULL,
  period_month    INTEGER,
  period_year     INTEGER NOT NULL,
  period_type     VARCHAR(20) DEFAULT 'monthly',
  total_tax       DECIMAL(14,2) NOT NULL,
  status          VARCHAR(20) DEFAULT 'draft',
  filed_date      DATE,
  reference_number VARCHAR(100),
  export_format   VARCHAR(20),
  export_url      TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

employees (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  code            VARCHAR(50),
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  khmer_name      VARCHAR(255),
  gender          VARCHAR(10),
  birth_date      DATE,
  phone           VARCHAR(50),
  email           VARCHAR(255),
  id_type         VARCHAR(50),
  id_number       VARCHAR(100),
  position        VARCHAR(255),
  department      VARCHAR(255),
  hire_date       DATE NOT NULL,
  termination_date DATE,
  status          VARCHAR(20) DEFAULT 'active',
  base_salary     DECIMAL(12,2) NOT NULL,
  bank_account    VARCHAR(100),
  bank_name       VARCHAR(100),
  nssf_number     VARCHAR(100),
  tax_dependents  INTEGER DEFAULT 0,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

attendance_records (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  employee_id     UUID FK NOT NULL,
  date            DATE NOT NULL,
  clock_in        TIMESTAMPTZ,
  clock_out       TIMESTAMPTZ,
  break_start     TIMESTAMPTZ,
  break_end       TIMESTAMPTZ,
  total_hours     DECIMAL(5,2),
  overtime_hours  DECIMAL(5,2) DEFAULT 0,
  status          VARCHAR(20) DEFAULT 'present',
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (employee_id, date)
);

payroll_periods (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  period_month    INTEGER NOT NULL,
  period_year     INTEGER NOT NULL,
  period_type     VARCHAR(20) DEFAULT 'monthly',
  start_date      DATE NOT NULL,
  end_date        DATE NOT NULL,
  payment_date    DATE,
  status          VARCHAR(20) DEFAULT 'open',
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

payroll_items (
  id                  UUID PK,
  payroll_period_id   UUID FK NOT NULL,
  employee_id         UUID FK NOT NULL,
  base_salary         DECIMAL(12,2) NOT NULL,
  allowances          JSONB DEFAULT '[]',
  overtime_amount     DECIMAL(12,2) DEFAULT 0,
  gross_salary        DECIMAL(12,2) NOT NULL,
  tos_amount          DECIMAL(12,2) DEFAULT 0,
  tofb_amount         DECIMAL(12,2) DEFAULT 0,
  nssf_employee       DECIMAL(12,2) DEFAULT 0,
  nssf_employer       DECIMAL(12,2) DEFAULT 0,
  deductions          JSONB DEFAULT '[]',
  net_salary          DECIMAL(12,2) NOT NULL,
  created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_coa_tenant_type ON chart_of_accounts(tenant_id, type);
CREATE INDEX idx_coa_code ON chart_of_accounts(code);
CREATE INDEX idx_journal_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_posted ON journal_entries(is_posted);
CREATE INDEX idx_jel_entry ON journal_entry_lines(journal_entry_id);
CREATE INDEX idx_invoices_tenant_status ON invoices(tenant_id, status);
CREATE INDEX idx_invoices_due ON invoices(due_date);
CREATE INDEX idx_invoices_source ON invoices(source_type, source_id);
CREATE INDEX idx_tax_records_period ON tax_records(tenant_id, tax_type, period_year, period_month);
CREATE INDEX idx_employees_tenant_status ON employees(tenant_id, status);
CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, date);
CREATE INDEX idx_payroll_period ON payroll_items(payroll_period_id);
CREATE UNIQUE INDEX idx_payroll_items_employee_period ON payroll_items(payroll_period_id, employee_id);
```

### 4.6 Payment Service — `payment_db`

```sql
payment_transactions (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  transaction_id  VARCHAR(100) UNIQUE NOT NULL,
  invoice_id      UUID,
  amount          DECIMAL(14,2) NOT NULL,
  currency        VARCHAR(3) DEFAULT 'USD',
  gateway         VARCHAR(50) NOT NULL,
  gateway_ref     VARCHAR(255),
  method          VARCHAR(50),
  status          VARCHAR(20) NOT NULL,
  customer_name   VARCHAR(255),
  customer_phone  VARCHAR(50),
  source_type     VARCHAR(50),
  source_id       UUID,
  metadata        JSONB DEFAULT '{}',
  paid_at         TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

gateway_logs (
  id              UUID PK,
  transaction_id  UUID FK,
  gateway         VARCHAR(50) NOT NULL,
  request_body    TEXT,
  response_body   TEXT,
  http_status     INTEGER,
  duration_ms     INTEGER,
  success         BOOLEAN,
  error_message   TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

refunds (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  transaction_id  UUID FK NOT NULL,
  amount          DECIMAL(14,2) NOT NULL,
  reason          TEXT,
  gateway_ref     VARCHAR(255),
  status          VARCHAR(20) DEFAULT 'pending',
  processed_at    TIMESTAMPTZ,
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

reconciliation_records (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  gateway         VARCHAR(50) NOT NULL,
  statement_date  DATE NOT NULL,
  total_expected  DECIMAL(14,2),
  total_matched   DECIMAL(14,2),
  total_unmatched DECIMAL(14,2),
  status          VARCHAR(20) DEFAULT 'pending',
  matched_count   INTEGER DEFAULT 0,
  unmatched_count INTEGER DEFAULT 0,
  processed_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_payments_invoice ON payment_transactions(invoice_id);
CREATE INDEX idx_payments_gateway_status ON payment_transactions(gateway, status);
CREATE INDEX idx_payments_paid ON payment_transactions(paid_at);
CREATE INDEX idx_payments_source ON payment_transactions(source_type, source_id);
CREATE INDEX idx_gateway_logs_transaction ON gateway_logs(transaction_id);
```

### 4.7 Reporting Service — `reporting_db`

```sql
report_definitions (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  code            VARCHAR(100),
  type            VARCHAR(50) NOT NULL,
  config          JSONB NOT NULL,
  is_system       BOOLEAN DEFAULT false,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

scheduled_reports (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  report_id       UUID FK NOT NULL,
  schedule_cron   VARCHAR(100) NOT NULL,
  recipients      JSONB NOT NULL,
  format          VARCHAR(20) DEFAULT 'pdf',
  is_active       BOOLEAN DEFAULT true,
  last_sent_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

report_executions (
  id              UUID PK,
  report_id       UUID FK NOT NULL,
  tenant_id       UUID FK NOT NULL,
  parameters      JSONB,
  status          VARCHAR(20) DEFAULT 'pending',
  output_url      TEXT,
  row_count       INTEGER,
  duration_ms     INTEGER,
  error_message   TEXT,
  requested_by    UUID FK,
  started_at      TIMESTAMPTZ,
  completed_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

dashboard_configs (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  name            VARCHAR(255) NOT NULL,
  layout          JSONB NOT NULL,
  is_default      BOOLEAN DEFAULT false,
  created_by      UUID FK,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ
);

aggregated_snapshots (
  id              UUID PK,
  tenant_id       UUID FK NOT NULL,
  snapshot_type   VARCHAR(50) NOT NULL,
  snapshot_date   DATE NOT NULL,
  data            JSONB NOT NULL,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (tenant_id, snapshot_type, snapshot_date)
);

CREATE INDEX idx_report_executions_report ON report_executions(report_id);
CREATE INDEX idx_report_executions_tenant ON report_executions(tenant_id, status);
CREATE INDEX idx_aggregated_type_date ON aggregated_snapshots(tenant_id, snapshot_type, snapshot_date DESC);
```

---

## 5. Schematic Diagram (ERD)

```
TENANTS (auth_db) ──────────────────────────────────────────────
  │
  ├──< USERS (auth_db)                1:N  ────  USER_ROLES
  │    │
  │    ├──< REFRESH_TOKENS (auth_db)
  │    ├──< ATTENDANCE_RECORDS (finance_db)  (employee check-in)
  │    └──< PURCHASE_ORDERS (inventory_db)   (created_by)
  │
  ├──< ROLES (auth_db)                1:N  ────  ROLE_PERMISSIONS
  │    └──< USER_ROLES (auth_db)
  │
  └──< each business table (via tenant_id)

PROPERTIES (property_db)
  │
  └──< UNITS                          1:N
       │
       ├──< LEASES                    1:N
       ├──< SCHEDULES                 1:N
       ├──< MAINTENANCE_TICKETS       1:N
       └──< INVOICES (finance_db)     (source_type='lease', source_id)

OUTLETS (restaurant_db)
  │
  ├──< TABLES                        1:N
  ├──< CATEGORIES                    1:N
  │    └──< MENU_ITEMS               1:N
  ├──< CUSTOMERS                     1:N
  ├──< ORDERS                        1:N
  │    └──< ORDER_ITEMS              1:N ──→ MENU_ITEMS
  ├──< RESERVATIONS                  1:N
  └──< INVOICES (finance_db)          (source_type='order', source_id)

PRODUCTS (inventory_db)
  │
  ├──< PRODUCT_CATEGORIES            1:N   (parent_id self-referencing)
  ├──< STOCK_ENTRIES                 1:N
  ├──< STOCK_EXITS                  1:N
  ├──< PURCHASE_ORDER_ITEMS          1:N
  │    └──< PURCHASE_ORDERS          M:1 ──→ SUPPLIERS
  └──< ORDER_ITEMS                   (product_id logical FK)

CHART_OF_ACCOUNTS (finance_db)
  │
  ├──< JOURNAL_ENTRY_LINES           1:N
  │    └──< JOURNAL_ENTRIES          1:N
  └──< INVOICE_ITEMS                 1:N

EMPLOYEES (finance_db)
  ├──< ATTENDANCE_RECORDS            1:N
  └──< PAYROLL_ITEMS                1:N ──→ PAYROLL_PERIODS

PAYMENT_TRANSACTIONS (payment_db)
  ├──< GATEWAY_LOGS                  1:N
  ├──< REFUNDS                      1:N
  └──< INVOICES (finance_db)         (invoice_id logical FK)
```

### Cross-Service Event Mapping

| Event | Publisher → Consumer | Logical Join |
|-------|---------------------|--------------|
| `sale.completed` | Restaurant → Finance | `orders.id → invoices.source_id` |
| `sale.completed` | Restaurant → Inventory | `order_items.menu_item_id → products.id` |
| `rent.invoice.generated` | Property → Payment | `invoices.id → payment_transactions.invoice_id` |
| `payment.received` | Payment → Finance | `payment_transactions.invoice_id → invoices.id` |
| `stock.received` | Inventory → Finance | `purchase_orders.id → journal_entries.reference_id` |
| `payroll.processed` | Finance → Reporting | `payroll_periods.id → aggregated_snapshots` |

---

## 6. Complete Project Structure

```
report-system/
│
├── pom.xml                                  # Parent POM
│
├── shared/
│   ├── common-dto/
│   │   └── src/main/java/com/reportsystem/shared/dto/
│   │       ├── event/
│   │       │   ├── SaleEvent.java
│   │       │   ├── PaymentEvent.java
│   │       │   ├── RentInvoiceEvent.java
│   │       │   ├── StockEvent.java
│   │       │   └── PayrollEvent.java
│   │       └── enums/
│   │           ├── TenantStatus.java
│   │           ├── InvoiceStatus.java
│   │           ├── TaxType.java
│   │           ├── ScheduleInterval.java
│   │           └── PaymentGateway.java
│   ├── tenant-context/
│   │   └── src/main/java/com/reportsystem/shared/
│   │       ├── TenantContext.java
│   │       ├── TenantFilter.java
│   │       └── TenantAware.java
│   ├── tax-engine/
│   │   └── src/main/java/com/reportsystem/shared/tax/
│   │       ├── TaxCalculator.java
│   │       ├── TaxBracket.java
│   │       ├── ToSCalculator.java
│   │       ├── VATCalculator.java
│   │       └── WHTCalculator.java
│   └── security-core/
│       └── src/main/java/com/reportsystem/shared/security/
│           ├── JwtTokenProvider.java
│           └── PermissionEvaluator.java
│
├── infrastructure/
│   ├── pom.xml
│   ├── eureka/
│   │   └── src/main/java/com/reportsystem/eureka/EurekaApplication.java
│   ├── gateway/
│   │   └── src/main/java/com/reportsystem/gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/{RouteConfig,CorsConfig,RateLimitConfig,SecurityConfig}.java
│   │       ├── filter/{JwtAuthGatewayFilter,TenantContextFilter,RequestLoggingFilter}.java
│   │       └── dto/ErrorResponse.java
│   └── config/
│
├── services/
│   ├── auth-service/                        # port 8081
│   │   └── src/main/java/com/reportsystem/auth/
│   │       ├── AuthServiceApplication.java
│   │       ├── domain/model/{Tenant,User,Role,Permission}.java
│   │       ├── domain/port/inbound/{AuthUseCase,TenantUseCase,UserUseCase}.java
│   │       ├── domain/port/outbound/{TenantRepository,UserRepository,RoleRepository}.java
│   │       ├── domain/service/{AuthService,JwtService,TenantService,UserService}.java
│   │       ├── infrastructure/persistence/entity/{TenantEntity,UserEntity,RoleEntity}.java
│   │       ├── infrastructure/persistence/repository/{JpaTenantRepository,JpaUserRepository}.java
│   │       ├── infrastructure/persistence/mapper/{TenantMapper,UserMapper}.java
│   │       ├── infrastructure/web/{AuthController,TenantController,UserController}.java
│   │       ├── infrastructure/web/dto/{LoginRequest,LoginResponse,RegisterRequest}.java
│   │       └── infrastructure/web/handler/GlobalExceptionHandler.java
│   │
│   ├── property-service/                    # port 8082
│   │   └── src/main/java/com/reportsystem/property/
│   │       ├── domain/model/{Property,Unit,Lease,Schedule,MaintenanceTicket}.java
│   │       ├── domain/service/{PropertyService,ScheduleService,LeaseService,MaintenanceService}.java
│   │       ├── infrastructure/event/{RentInvoiceEventPublisher,TenantEventPublisher}.java
│   │       └── infrastructure/web/{PropertyController,UnitController,LeaseController,ScheduleController,MaintenanceController}.java
│   │
│   ├── restaurant-service/                  # port 8083
│   │   └── src/main/java/com/reportsystem/restaurant/
│   │       ├── domain/model/{Outlet,Table,MenuItem,Order,OrderItem,Customer,Reservation}.java
│   │       ├── domain/service/{PosService,OrderService,MenuService,CustomerService,KdsService}.java
│   │       ├── infrastructure/event/SaleEventPublisher.java
│   │       ├── infrastructure/websocket/KitchenWebSocketHandler.java
│   │       └── infrastructure/web/{PosController,MenuController,OrderController,CustomerController}.java
│   │
│   ├── inventory-service/                   # port 8084
│   │   └── src/main/java/com/reportsystem/inventory/
│   │       ├── domain/model/{Product,Supplier,PurchaseOrder,StockEntry,Warehouse}.java
│   │       ├── domain/service/{StockService,SupplierService,PurchaseOrderService,StockAlertService}.java
│   │       ├── infrastructure/event/{StockEventPublisher,StockEventConsumer}.java
│   │       └── infrastructure/web/{ProductController,SupplierController,PurchaseOrderController}.java
│   │
│   ├── finance-service/                     # port 8085
│   │   └── src/main/java/com/reportsystem/finance/
│   │       ├── domain/model/{Account,JournalEntry,Invoice,TaxRecord,Employee,PayrollItem}.java
│   │       ├── domain/service/{AccountingService,InvoiceService,TaxService,PayrollService,GdtExportService}.java
│   │       ├── infrastructure/event/{SaleEventConsumer,RentEventConsumer,PaymentEventConsumer,PayrollEventPublisher}.java
│   │       └── infrastructure/web/{AccountingController,TaxController,PayrollController}.java
│   │
│   ├── payment-service/                     # port 8086
│   │   └── src/main/java/com/reportsystem/payment/
│   │       ├── domain/model/{Transaction,Refund,Reconciliation}.java
│   │       ├── domain/port/outbound/PaymentGatewayPort.java
│   │       ├── domain/service/{PaymentService,GatewayRouter,ReconciliationService}.java
│   │       ├── infrastructure/gateway/{AbaPayWayAdapter,WingAdapter,PiPayAdapter,CashAdapter}.java
│   │       ├── infrastructure/event/PaymentEventPublisher.java
│   │       └── infrastructure/web/PaymentController.java
│   │
│   └── reporting-service/                   # port 8087
│       └── src/main/java/com/reportsystem/reporting/
│           ├── domain/model/{ReportDefinition,ReportExecution,DashboardConfig,AggregatedSnapshot}.java
│           ├── domain/service/{ReportService,DashboardService,PdfExportService,CsvExportService,AggregationService}.java
│           ├── infrastructure/event/*EventConsumer.java
│           └── infrastructure/web/{ReportController,DashboardController}.java
│
├── frontend/
│   └── report-system-web/
│       ├── package.json
│       ├── nuxt.config.ts
│       ├── Dockerfile
│       ├── app/
│       │   ├── app.vue
│       │   ├── layouts/{default,app,admin}.vue
│       │   ├── pages/
│       │   │   ├── index.vue
│       │   │   ├── login.vue
│       │   │   ├── register.vue
│       │   │   ├── app/
│       │   │   │   ├── dashboard.vue
│       │   │   │   ├── profile.vue
│       │   │   │   ├── property/{index,[id],schedule,maintenance}.vue
│       │   │   │   ├── restaurant/{pos,menu,tables,customers,kds}.vue
│       │   │   │   ├── inventory/{index,products,suppliers,purchase-orders}.vue
│       │   │   │   ├── finance/{dashboard,chart-of-accounts,journal,invoices,tax/{dashboard,vat,filings},payroll/{employees,attendance,run-payroll}}.vue
│       │   │   │   ├── reports/{occupancy,revenue,sales}.vue
│       │   │   │   └── admin/{users,roles,audit-log,settings}.vue
│       │   │   └── 404.vue
│       │   ├── middleware/{auth,tenant}.ts
│       │   ├── plugins/{axios,pinia}.ts
│       │   ├── shared/types/{auth,property,restaurant,finance,payment}.ts
│       │   └── server/api/{auth/*,proxy/[...path].ts}
│       │
│       └── layers/
│           ├── base/
│           │   ├── nuxt.config.ts
│           │   └── app/components/{BaseButton,BaseCard,BaseModal,BaseTable,BaseInput,BaseSelect}.vue
│           ├── core/
│           │   ├── nuxt.config.ts
│           │   └── app/components/{AppHeader,AppSidebar,AppBreadcrumb,TenantSwitcher,UserMenu}.vue
│           ├── property/
│           │   ├── nuxt.config.ts
│           │   └── app/components/{PropertyCard,PropertyForm,UnitCard,ScheduleCalendar,MaintenanceTicket}.vue
│           ├── restaurant/
│           │   ├── nuxt.config.ts
│           │   └── app/components/{PosGrid,PosCart,PosPaymentModal,TableMap,KdsOrderCard,ReservationCalendar}.vue
│           ├── inventory/
│           │   ├── nuxt.config.ts
│           │   └── app/components/{StockTable,ProductForm,SupplierForm,PurchaseOrderForm,LowStockAlert}.vue
│           └── finance/
│               ├── nuxt.config.ts
│               └── app/components/
│                   ├── accounting/{AccountTree,JournalEntryForm,TrialBalanceTable,InvoiceForm}.vue
│                   ├── tax/{TaxSummaryCard,TaxFilingForm,VatReturnForm,TosBracketTable,GdtExportButton}.vue
│                   ├── payroll/{EmployeeForm,AttendanceSheet,PayrollRunForm,PayslipViewer}.vue
│                   └── reports/{ProfitLossChart,RevenueChart,ArAgingTable}.vue
│
├── scripts/
│   ├── init-dbs.sh
│   ├── wait-for-services.sh
│   └── seed-dev-data.sh
│
├── docker/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   └── .env.example
│
├── .github/workflows/
│   ├── ci-auth.yml
│   ├── ci-property.yml
│   ├── ci-restaurant.yml
│   ├── ci-inventory.yml
│   ├── ci-finance.yml
│   ├── ci-payment.yml
│   ├── ci-reporting.yml
│   └── ci-gateway.yml
│
└── AGENTS.md
```


---

## 7. Execution Plan — 8 Sprints (4 Months)

### Sprint 1: Foundation & Core Infrastructure (Weeks 1-2)

**Backend:**
- Set up parent POM with dependency management (Spring Boot 3.x, Spring Cloud, Kafka, PostgreSQL, Resilience4j)
- Create shared modules: common-dto, tenant-context, security-core, tax-engine
- Scaffold Eureka server (discovery) and Spring Cloud Gateway (routing, JWT filter, CORS, rate limiting)
- Scaffold Auth Service with Hexagonal architecture: Tenant entity, User entity, Role/Permission entities
- Implement domain service layer: AuthService (register, login, refresh token), JwtService (token generation/validation), TenantService (CRUD), UserService (CRUD, role assignment)
- Implement JPA repositories, Flyway migration for `auth_db` (tenants, users, roles, permissions, role_permissions, user_roles, refresh_tokens)
- Implement REST controllers: AuthController (POST /auth/login, /auth/register, /auth/refresh), TenantController, UserController
- Docker Compose for dev (PostgreSQL x7, Kafka, Redis, Eureka, Gateway, Auth Service)

**Frontend:**
- Scaffold Nuxt 3 project with TypeScript, Tailwind CSS, Pinia
- Implement base UI layer (BaseButton, BaseCard, BaseModal, BaseTable, BaseInput, BaseSelect)
- Implement core layout (AppHeader, AppSidebar, auth middleware, tenant context)
- Login & register pages wired to Auth Service via server proxy
- Landing page, 404 page

**DevOps:**
- `.env.example` with all service ports and secrets
- `init-dbs.sh` script to create all 7 databases
- GitHub Actions CI for auth-service (compile, test, lint)

**Deliverables:** Eureka running, Gateway proxying, Auth Service with JWT login/register working, Nuxt app with login flow, Docker Compose bringing up 10 containers.

---

### Sprint 2: Property & Restaurant Services (Weeks 3-4)

**Property Service (Spring Boot, port 8082):**
- Domain models: Property, Unit, Lease, Schedule, MaintenanceTicket
- Services: PropertyService (CRUD + unit management), ScheduleService (recurring engine: hourly/daily/weekly/monthly/yearly), LeaseService (move-in/move-out lifecycle), MaintenanceService (ticketing workflow)
- Controllers: full CRUD for properties, units, leases, schedules, maintenance
- Flyway migration for `property_db`
- Event publishers: RentInvoiceEvent (Kafka → Finance), TenantEvent (moved-in/moved-out)
- Unit-level schedule validation (no double-booking), lease overlap detection

**Restaurant Service (Spring Boot, port 8083):**
- Domain models: Outlet, Table, MenuItem, Order, OrderItem, Customer, Reservation
- Services: PosService (order lifecycle), OrderService (dine-in/takeaway/delivery), MenuService (category CRUD, item CRUD, modifiers), CustomerService (CRM, loyalty tier), KdsService (WebSocket push)
- Controllers: POS, menu CRUD, order management, customer management
- WebSocket handler for Kitchen Display System (real-time order push)
- Flyway migration for `restaurant_db`
- Event publisher: SaleEvent (Kafka → Inventory, Finance)
- QR code generation per table

**Frontend (Property):**
- Property list/detail pages, Unit list/cards, Lease management page
- Schedule calendar view, Maintenance ticket board

**Frontend (Restaurant):**
- POS grid with category tabs, cart panel, payment modal
- Menu management page, Table map, Customer list
- KDS real-time order display page

**Deliverables:** Property Service with full CRUD + schedule engine. Restaurant Service with POS order flow + KDS WebSocket. Docker Compose expanded to 12 containers.

---

### Sprint 3: Inventory & Payment Services (Weeks 5-6)

**Inventory Service (Spring Boot, port 8084):**
- Domain models: Product, Supplier, PurchaseOrder, StockEntry, StockExit, Warehouse, ProductCategory
- Services: StockService (entry/exit with FIFO/FEFO tracking batch support), SupplierService (CRUD, payment terms, vendor performance), PurchaseOrderService (PO lifecycle: draft→sent→received→completed, partial receipt), StockAlertService (threshold monitoring, low-stock events)
- Controllers: full CRUD for products, suppliers, POs, stock adjustments
- Flyway migration for `inventory_db`
- Event publisher: StockReceivedEvent (Kafka → Finance), LowStockAlert (Kafka → Restaurant/Property)
- Kafka consumer: SaleEvent (deduct stock from restaurant orders)
- Barcode/QR scanning support for stock entry/exit

**Payment Service (Spring Boot, port 8086):**
- Domain models: Transaction, GatewayLog, Refund, ReconciliationRecord
- Services: PaymentService (create transaction, process via gateway router), GatewayRouter (strategy pattern routing to ABA/Wing/Pi Pay/Cash), ReconciliationService (match transactions vs gateway statements)
- Gateway adapters implementing `PaymentGatewayPort`: AbaPayWayAdapter (REST integration), WingAdapter (API), PiPayAdapter (API), CashAdapter (offline stub with manual confirmation)
- Controllers: POST /payments/process, GET /payments/{id}, POST /payments/refund, GET /payments/reconciliation
- Flyway migration for `payment_db`
- Event publisher: PaymentReceived, PaymentFailed (Kafka → Finance, Property, Restaurant)

**Frontend (Inventory):**
- Product list/form with category tree, Supplier management, PO creation
- Stock adjustment form, Low-stock alert badge on sidebar

**Frontend (Payment):**
- Payment method selection in POS modal (ABA/Wing/Pi Pay/Cash)
- Payment history page with gateway filter
- Manual cash payment confirmation flow

**Deliverables:** Inventory Service with full stock management + PO workflow. Payment Service with ABA/Wing/Pi Pay/Cash gateways. 14 containers running.

---

### Sprint 4: Finance Service Core (Weeks 7-8)

**Finance Service (Spring Boot, port 8085) — Part 1:**
- Domain models: Account (chart of accounts with tree structure), JournalEntry, JournalEntryLine, Invoice, InvoiceItem
- Services: AccountingService (double-entry engine: post journal entries, validate debit=credit), InvoiceService (AR/AP lifecycle: generate, send, record payment, aging report)
- Controllers: Accounts CRUD, Journal entries (create, post, list), Invoices (generate, list, aging report)
- Flyway migration for `finance_db` (chart_of_accounts, journal_entries, journal_entry_lines, invoices, invoice_items)
- Kafka consumers: SaleEventConsumer (auto-generate invoices & journal entries from restaurant sales), RentInvoiceEventConsumer (generate rent invoices from property leases)
- GDT-compatible invoice numbering format

**Frontend (Finance):**
- Chart of Accounts tree view with CRUD
- Journal entry form (multi-line with debit/credit validation, auto-balance check)
- Invoice list with status filters, Invoice creation form
- Account tree draggable parent/child assignment

**Deliverables:** Double-entry accounting working end-to-end with auto-posting from Kafka events. Invoice generation for both property (rent) and restaurant (POS sales). 15 containers.

---

### Sprint 5: Payroll & Tax Engine (Weeks 9-10)

**Finance Service — Part 2 (Payroll):**
- Domain models: Employee, AttendanceRecord, PayrollPeriod, PayrollItem
- Services: PayrollService (run payroll: compute gross, allowances, overtime, deductions, TOS, NSSF), GdtExportService (generate CSV/XML for GDT e-filing)
- Controllers: Employee CRUD, Attendance (clock-in/out, manual entry, import), Payroll run (generate period, compute all, preview, confirm)
- Flyway additional tables: employees, attendance_records, payroll_periods, payroll_items
- NSSF computation (employee 2% + employer 2.6% for occupational risk)
- Payslip generation (PDF via iText or JasperReports)
- Event publisher: PayrollProcessedEvent (Kafka → Reporting)

**Tax Engine (shared module enhancement):**
- VATCalculator: 10% VAT on taxable goods/services, input VAT credit tracking
- ToSCalculator: progressive tax on salary (₭0–1.3M: 0%, 1.3M–2M: 5%, 2M–8.5M: 10%, 8.5M–12.5M: 15%, >12.5M: 20%)
- WHTCalculator: 15% on rent, 10% on interest/royalties, 2% on goods/services from non-residents
- TaxRecord aggregation by period for monthly/quarterly/annual reports

**Tax filing module:**
- Generate VAT return summary (monthly)
- Generate TOI/PTOI summary (annual)
- Generate WHT certificate per supplier (monthly)
- Export to GDT-compatible CSV/XML

**Frontend (Payroll & Tax):**
- Employee list/form with Khmer name field, NSSF number
- Attendance sheet (calendar grid), clock-in/out button
- Payroll run wizard (select period → preview → confirm)
- Payslip viewer with print button
- Tax dashboard (VAT, WHT, TOS summaries per period)
- VAT return form with auto-populated fields
- Tax filing history with download buttons (CSV/XML)
- GDT export button per tax type

**Deliverables:** Full payroll processing with Cambodia tax compliance. Employee attendance tracking, NSSF contributions, payslip generation. GDT e-filing exports for VAT, WHT, TOS. 15 containers.

---

### Sprint 6: Reporting & OCR AI (Weeks 11-12)

**Reporting Service (Spring Boot, port 8087):**
- Domain models: ReportDefinition, ScheduledReport, ReportExecution, DashboardConfig, AggregatedSnapshot
- Services: ReportService (parameterized report generation), DashboardService (widget layout CRUD), PdfExportService (iText/JasperReports), CsvExportService, AggregationService (daily snapshots from Kafka events)
- Kafka consumers: all events (sale, payment, rent, stock, payroll) for aggregation
- Pre-built reports: Occupancy rate (property), Revenue by outlet (restaurant), P&L summary, AR aging, Tax summary (all types), Daily sales, Stock valuation
- Scheduled reports via cron triggers (email PDF attachments)
- Aggregated snapshots: daily occupancy, daily sales, daily cash position

**AI — OCR for Invoices/Receipts:**
- Integration with Google Cloud Vision / Tesseract for receipt scanning
- Image preprocessing (Kotlin/Java image processing for Khmer OCR preprocessing)
- Receipt parsing: extract vendor, date, line items, totals, tax amounts
- Auto-create journal entries from scanned receipts
- Receipt-to-PO matching (3-way match: PO → Receipt → Invoice)
- Store OCR job status and extracted data in reporting_db

**Controllers:**
- Report CRUD, Execution trigger, Schedule management
- Dashboard layout CRUD
- POST /reports/ocr/upload (multipart image → OCR → parse → preview → confirm)
- GET /reports/occupancy, /reports/revenue, /reports/pnl

**Frontend (Reports & OCR):**
- Dashboard builder (drag-drop widgets from pre-built list)
- Report list with schedule config (cron editor, email recipients)
- Report execution history with download links
- Pre-built dashboard layouts: Property Manager, Restaurant Owner, Accountant, CEO
- OCR scan page: upload receipt, preview parsed data, confirm → auto-post journal entry
- Charts: occupancy trend (area chart), revenue by outlet (bar), P&L (waterfall), AR aging (table+bar)

**Deliverables:** Reporting Service with 10+ pre-built reports, scheduled PDF email delivery, OCR AI for invoice/receipt scanning with auto-posting. 16 containers.

---

### Sprint 7: Admin Tools, Monitoring & Polish (Weeks 13-14)

**Admin Features:**
- Tenant admin panel (manage users, roles, permissions per tenant)
- Audit log (track all CRUD operations across services via Kafka audit topic)
- System settings (global config, feature toggles)
- Subscription management (tier limits: max units, outlets, users)
- Import/export: CSV upload for chart of accounts, products, employees

**Monitoring & Observability:**
- Prometheus metrics per service (request count, latency, error rate, JVM)
- Grafana dashboards (service health, business KPIs, Kafka consumer lag)
- Zipkin distributed tracing setup
- Health check endpoints (`/actuator/health`, `/actuator/info`)
- Log aggregation: structured JSON logging (Logstash) → ELK or Loki
- Alert rules: service down, high latency, low stock, unpaid invoices due

**Cross-cutting polish:**
- Rate limiting on gateway (per tenant, per endpoint)
- Circuit breaker patterns (Resilience4j) between services
- Retry + dead letter topics for Kafka consumers
- Validation consistency: Jakarta Validation annotations on all DTOs
- API error standardization (consistent error response JSON format)
- i18n support in API messages (English + Khmer)

**Frontend Polish:**
- Responsive layout (mobile-friendly POS, table maps)
- Loading skeletons, empty states, error states
- Form validation with inline messages (Khmer + English)
- Toast notifications for success/error
- Keyboard shortcuts for POS (F1-F8 for categories, Enter for payment)
- Khmer language toggle (localStorage-persisted)

**Deliverables:** Admin panel functional. Monitoring stack live (Prometheus+Grafana+Zipkin). Circuit breakers and rate limiters active. Frontend polished with Khmer i18n.

---

### Sprint 8: Testing, Docs, Deployment & Buffer (Weeks 15-16)

**Testing:**
- Unit tests: domain services (JUnit 5 + Mockito), target 80%+ coverage
- Integration tests: repositories (Testcontainers for PostgreSQL), controllers (@WebMvcTest), Kafka consumers (@EmbeddedKafka)
- Contract tests: REST API contracts between services (Spring Cloud Contract)
- End-to-end tests: critical flows (tenant signup → property → lease → invoice → payment → accounting)
- Load test: K6 script for POS (100 concurrent orders) and payment flows
- Security audit: JWT expiry, SQL injection, XSS, CORS config review

**Documentation:**
- API documentation (OpenAPI 3.1 per service, aggregated via Gateway)
- Local dev setup guide (README.md)
- Architecture decision records (ADR) for: microservice decomposition, event catalog, gateway routing, payment gateway selection
- Docker Compose README (how to start dev, prod)
- K8s migration guide (document differences: ConfigMaps, Secrets, Ingress, HPA, PVC)

**Deployment:**
- Docker Compose production profile (with resource limits, restart policies, health checks)
- Cloud deployment guide (DO/AWS): VPC, RDS, ElastiCache, MSK/LMS, ECS/EKS
- Backup strategy: pg_dump cron, MinIO S3, retention policy
- SSL/TLS with Let's Encrypt + Caddy or Nginx reverse proxy
- CI/CD pipeline finalization: build → test → docker build → push → deploy

**Buffer:**
- Bug fixes from integration testing
- Performance optimization (slow queries, N+1, index tuning)
- UX refinements from feedback
- Final security review

**Deliverables:** 80%+ test coverage, full API docs, production-ready Docker Compose, K8s migration docs, deployment guide. **MVP complete.**

---

## 8. Cambodia Tax System — Complete Reference

| # | Tax | Rate | Period | Applies To |
|---|-----|------|--------|------------|
| 1 | **VAT** | 10% | Monthly | Goods/services > $62.5K/yr revenue; all restaurant sales, property management fees |
| 2 | **TOI** (Tax on Income) | 20% | Annual | Corporate net profit |
| 3 | **PTOI** (Prepaid TOI) | 1% | Monthly | On gross revenue (vs annual TOI); 2-step with year-end true-up |
| 4 | **ToS** (Tax on Salary) | 0–20% | Monthly | Progressive: 0% (≤₭1.3M), 5% (₭1.3M–2M), 10% (₭2M–8.5M), 15% (₭8.5M–12.5M), 20% (>₭12.5M) |
| 5 | **ToFB** (Fringe Benefit Tax) | 20% | Monthly | Non-cash benefits (housing, car, etc.) ≥ ₭1M/month |
| 6 | **WHT** (Withholding Tax) | 2–15% | Monthly | 15% rent/interest/royalties; 10% management fees; 2% goods/services from non-residents |
| 7 | **Property Rental Tax** | 10% | Monthly | Rental income from property (withheld by tenant if commercial) |
| 8 | **Property Tax** | 0.1% | Annual | On property value > ₭1B (~$250K); assessed by GDT |
| 9 | **Accommodation Tax** | 2–10% | Monthly | Hotels/guesthouses: 2% on room revenue + 10% on F&B/other services |
| 10 | **Specific Tax** | 3–90% | Monthly | On specific goods (alcohol, tobacco, cars, fuel, etc.) |
| 11 | **Patent Tax** | Fixed | Annual | Business license tax: $100–$3,000 depending on turnover bracket |
| 12 | **Signboard Tax** | Fixed | Annual | Per sign; varies by size/location; typically $50–$500 |
| 13 | **Public Lighting Tax** | 3% | Monthly | On accommodation room revenue only |
| 14 | **Minimum Tax** | 1% | Annual | If TOI < 1% of revenue; pay 1% of revenue instead |

**Key Compliance Notes:**
- NSSF: 2% employee + 2.6% employer (occupational risk) on gross salary
- VAT: input credits deductible; must file by 20th of following month
- TOI: estimated 1% PTOI paid monthly; annual return by March 31
- WHT: certificates must be issued to payees; filed monthly
- E-filing: via GDT portal; CSV/XML export required in our system
- Double records: many Cambodia businesses keep USD + KHR ledgers
- Tax year: calendar year (Jan 1 – Dec 31)

---

## 9. AI Integration — Scope for MVP

### Invoice/Receipt OCR (MVP)
**Goal:** Scan supplier invoices and receipts → auto-create journal entries and match to POs.

**Tech Stack:** Google Cloud Vision API (primary) + Tesseract fallback.

**Pipeline:**
1. User uploads image/PDF → Reporting Service
2. Image preprocessing: rotate, deskew, contrast enhance (for Khmer text legibility)
3. OCR extraction (Google Vision → structured JSON with confidence scores)
4. Field parsing: vendor name, date, line items, unit prices, quantities, totals, tax amounts
5. Confidence check: fields > 80% confidence auto-mapped; < 80% flagged for manual review
6. Preview screen: user reviews extracted data, makes corrections
7. On confirm:
   - Create/update supplier (if new)
   - Create purchase journal entry (DR Inventory/Expense, CR AP/VAT Input)
   - If PO exists: 3-way match (PO vs Receipt vs Invoice)
   - Create stock entry if inventory items
8. Store OCR job + result for audit trail

**AI Infrastructure in MVP:**
- Reporting Service `POST /reports/ocr/upload`
- Image stored in MinIO with reference in report_executions
- Async processing (Kafka: `ocr.requested` / `ocr.completed`)
- Batch mode: upload multiple receipts → process queue

**Khmer Language Challenge:**
- Google Vision supports Khmer (km) with moderate accuracy
- Preprocessing improves Khmer character recognition
- Fallback: manual data entry for low-confidence extractions
- Future: train custom model on Khmer invoices after MVP

### Deferred AI Features (Post-MVP)
- Demand forecasting for inventory (ARIMA/Prophet)
- Tenant payment behavior scoring (ML classification)
- Smart maintenance prioritization (NLP on ticket descriptions)
- Menu item recommendation engine (collaborative filtering)
- Anomalous transaction detection (ML on payment patterns)

---

## 10. Infrastructure & Hosting

### Development (Docker Compose)

| Service | Image | Port | CPU | Memory |
|---------|-------|------|-----|--------|
| PostgreSQL | postgres:16-alpine | 5432 | shared | 2GB |
| Kafka | confluentinc/cp-kafka:7.5 | 9092 | shared | 2GB |
| Redis | redis:7-alpine | 6379 | shared | 256MB |
| Zipkin | openzipkin/zipkin:latest | 9411 | shared | 512MB |
| Eureka | spring boot (1 instance) | 8761 | 0.25 | 512MB |
| Gateway | spring boot (1 instance) | 8080 | 0.5 | 512MB |
| Auth Service | spring boot (1–2) | 8081 | 0.5 | 512MB |
| Property Service | spring boot (1–2) | 8082 | 0.5 | 768MB |
| Restaurant Service | spring boot (1–2) | 8083 | 0.5 | 768MB |
| Inventory Service | spring boot (1–2) | 8084 | 0.5 | 512MB |
| Finance Service | spring boot (1–2) | 8085 | 0.5 | 768MB |
| Payment Service | spring boot (1–2) | 8086 | 0.5 | 512MB |
| Reporting Service | spring boot (1–2) | 8087 | 0.5 | 768MB |
| MinIO | minio/minio:latest | 9000 | shared | 512MB |
| Nuxt.js | node:20-alpine | 3000 | 0.5 | 512MB |
| Nginx | nginx:alpine | 80/443 | shared | 128MB |
| Prometheus | prom/prometheus:latest | 9090 | shared | 512MB |
| Grafana | grafana/grafana:latest | 3001 | shared | 256MB |

### Estimated Hosting Cost (DO Droplet or AWS EC2)

| Tier | Monthly Cost | Spec | Max Tenants |
|------|-------------|------|-------------|
| Starter | ~$80/mo | 2×4GB VMs (Docker Swarm) | 10–20 tenants |
| Business | ~$150/mo | 3×8GB VMs (Docker Swarm) | 50–100 tenants |
| Scale | ~$400/mo | 5×16GB VMs (K8s cluster) | 200–500 tenants |

**Cost breakdown (Business tier):**
- 2×8GB droplets (~$48/mo each) = $96
- Managed PostgreSQL = $15/mo
- Managed Kafka (Confluent Cloud basic) = $30/mo
- Object storage (S3/Spaces) = $5/mo
- Load balancer = $12/mo
- Domain + SSL = ~$10/mo
- **Total ≈ $168/mo**

---

## 11. Pricing Strategy

| Tier | Price | Target | Limits |
|------|-------|--------|--------|
| **Starter** | $29/month | Small landlords, single-outlet restaurants | 1 location, 10 units, 50 menu items, 1 user |
| **Business** | $79/month | Growing portfolios, multi-outlet F&B | 5 locations, 100 units, 500 menu items, 10 users |
| **Enterprise** | $199/month | Property managers, chains, hospitality groups | Unlimited locations, unlimited units, unlimited users, priority support |

**Add-on pricing:**
- Extra location (beyond tier): +$20/mo per location
- OCR AI scanning: first 100 scans/mo included; +$10 per 100 scans
- SMS notifications (rent reminders, promo): $0.05/sms
- White-label domain: +$30/mo

**Revenue projections:**
- **Conservative:** 20 tenants @ avg $49 = **$11,760/yr**
- **Moderate:** 100 tenants @ avg $69 = **$82,800/yr**
- **Growth:** 500 tenants @ avg $89 = **$534,000/yr**

**Revenue vs Cost Recovery:**
- At Business tier ($79/mo): need **3 subscribers** to break even on infra ($168/mo → ~6 weeks to recover at $79/tier)
- At Enterprise tier ($199/mo): **1 subscriber** covers infra

---

## 12. Team Estimate

| Role | Engagement | Duration | All-in Cost (Cambodia) |
|------|-----------|----------|----------------------|
| Senior Java Developer (Spring Boot, Kafka) | Full-time | 4 months | $5,000–$8,000 |
| Mid-Level Java Developer | Full-time | 4 months | $3,000–$5,000 |
| Nuxt.js/Vue.js Developer | Full-time | 4 months | $3,000–$5,000 |
| UI/UX Designer (part-time) | 20 hrs/week | 2 months | $1,000–$2,000 |
| DevOps (part-time) | 10 hrs/week | 2 months | $1,000–$2,000 |
| QA / Tester (part-time) | 20 hrs/week | 2 months | $1,000–$2,000 |

**Total estimated:** **$14,000–$24,000** for 4-month MVP build.

**Alternative: solo full-stack developer + AI-assisted coding:**
- 1 senior full-stack dev (Java + Vue) + AI tools → $4,000–$6,000/mo × 5 months = $20,000–$30,000

---

## 13. Development Workflow

### Branch Strategy
```
main          ← production-ready
├── develop   ← integration branch
│   ├── feature/auth-service
│   ├── feature/property-service
│   ├── feature/restaurant-service
│   ├── feature/inventory-service
│   ├── feature/finance-service
│   ├── feature/payment-service
│   ├── feature/reporting-service
│   └── feature/frontend-*
└── hotfix/*  ← urgent production fixes
```

### PR Workflow
1. Create feature branch from `develop`
2. Implement + write unit/integration tests
3. Run CI (compile, test, lint)
4. Create PR to `develop` → code review
5. Merge → CI runs again on `develop`
6. Release branch from `develop` → merge to `main` → deploy

### Commit Convention (`[service] type: description`)
- `[auth] feat: add refresh token rotation`
- `[property] fix: lease overlap detection off-by-one`
- `[restaurant] refactor: extract payment from POS controller`
- `[finance] test: add payroll computation parameterized tests`
- `[infra] chore: update docker-compose to PostgreSQL 16`
- `[frontend] feat: add Khmer locale toggle`

### CI Pipeline (GitHub Actions per service)
```
trigger: push to feature/* or develop
  ↓
compile (Maven)
  ↓
unit tests (JUnit 5)
  ↓
integration tests (Testcontainers)
  ↓
lint (Checkstyle)
  ↓
build Docker image
  ↓
(optional) push to registry
```

### Pre-commit Checklist
- `mvn compile` passes
- `mvn test` passes
- `mvn checkstyle:check` passes
- No secrets committed (`.env` files, `application.yml` with real passwords)
- Migration added for any schema change
- Kafka topic documented for new events
- API documented with OpenAPI annotations

---

## 14. Keycloak SSO + Multi-Branch Architecture (Sprints 9–11)

> **Status:** Architecture definition, **not yet implemented** as of 2026-06-18.
> Three new sprints added after Sprint 8. Affects all 7 services, gateway, frontend, and infrastructure.

### 14.1 Goals & Scope

| Goal | Description | Why |
|------|-------------|-----|
| **Keycloak SSO** | Replace the in-house `auth-service` JWT issuance with Keycloak as the identity provider. One Keycloak **realm per tenant** for full isolation. | Enterprise customers require SSO (SAML/OIDC), MFA, password policies, audit logs, social login, user federation (LDAP/AD). Building these in-house is risky and slow. |
| **Multi-branch** | Add `Branch` as a first-class entity. **All** domain entities get `branch_id`. Users can belong to multiple branches with different roles per branch. | Target market is chains with 100+ locations. Without branches, the data model collapses to single-location only. |
| **Cross-branch transfers** | Inventory can be moved between branches; financial transactions can be inter-branch (e.g., one branch paying another). | Real chains have warehouses, central kitchens, inter-store transfers. Required for accurate COGS and stock-on-hand. |

### 14.2 Updated Architecture

```
                ┌──────────────────────────────────────────┐
                │         Nuxt.js 3 Frontend (:3000)       │
                │  keycloak-js adapter + branch context    │
                │  Pinia store: auth, currentBranch        │
                └────────────────┬─────────────────────────┘
                                 │ OIDC redirect / Bearer
                                 ▼
   ┌──────────────────────────────────────────────────────┐
   │           Spring Cloud Gateway (:8080)                │
   │   • OIDC token validation via Keycloak JWK            │
   │   • Headers: X-Tenant-Id, X-Branch-Ids, X-User-Id,    │
   │     X-User-Roles (parsed from JWT)                    │
   │   • Branch selector from header or `?branchId=...`    │
   └────┬──────┬──────┬──────┬──────┬──────┬───────┬───────┘
        │      │      │      │      │      │       │
        ▼      ▼      ▼      ▼      ▼      ▼       ▼
      auth  property restaurant inventory finance payment reporting
        │      │      │      │      │      │       │
        ▼      ▼      ▼      ▼      ▼      ▼       ▼
      (no DB) branch branch   branch   branch   branch   branch
              DB     DB       DB       DB       DB       DB
                 (every table has tenant_id + branch_id)
                                 │
                ┌────────────────┼────────────────┐
                ▼                ▼                ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │  Keycloak    │  │   Eureka     │  │   Kafka      │
        │  :8180       │  │   :8761      │  │   :9092      │
        │              │  │              │  │              │
        │ Realm-per-   │  │              │  │ Topics:      │
        │ tenant:      │  │              │  │ • branch.*   │
        │ • master     │  │              │  │ • transfer.* │
        │   (admin)    │  │              │  │ • auth.*     │
        │ • {slug}     │  │              │  │              │
        │   (tenant)   │  │              │  │              │
        └──────────────┘  └──────────────┘  └──────────────┘
```

### 14.3 Keycloak Realm-per-Tenant Model

**One realm per tenant** — chosen for full data isolation, per-tenant password policies, independent SSO config, and clean admin delegation.

```
Keycloak
├── master realm
│   └── admin user (super-admin from Report System)
│
├── realm: demo-corp          ← tenant slug = realm name
│   ├── users: admin@demo, ...
│   ├── roles: BRANCH_MANAGER, CASHIER, ...
│   ├── clients:
│   │   ├── report-system-web   (public, PKCE flow)
│   │   ├── report-system-api   (bearer-only)
│   │   └── report-system-sync  (confidential, for backend service accounts)
│   ├── identity providers: Google, Facebook, etc. (optional)
│   ├── user federation: LDAP/AD (optional)
│   └── events: enabled (audit log)
│
├── realm: acme-holdings
│   └── ... isolated config ...
│
└── realm: ...
```

**Realm bootstrap flow:**
1. Tenant signs up via `/auth/register` (or admin creates them)
2. Auth-service calls Keycloak Admin API: `POST /admin/realms` with new realm config
3. Auth-service creates an initial realm admin user, sends invite email
4. Tenant admin logs into Keycloak admin console (`/auth/admin/{tenant}/...`) to manage their users
5. Auth-service continues to own: tenant metadata, billing, plan limits, branches
6. Keycloak owns: user credentials, roles, sessions, MFA, SSO, audit

**JWT claims (per realm):**
```json
{
  "sub": "f4106d84-8024-4cda-b82e-4ca04d120330",
  "tenant_id": "00000000-0000-0000-0000-000000000001",
  "branch_ids": ["branch-uuid-a", "branch-uuid-b"],
  "current_branch": "branch-uuid-a",
  "branch_roles": {
    "branch-uuid-a": ["BRANCH_MANAGER"],
    "branch-uuid-b": ["CASHIER"]
  },
  "email": "admin@demo.com",
  "name": "Admin User",
  "realm_access": {
    "roles": ["user", "offline_access"]
  },
  "iss": "https://keycloak:8180/realms/demo-corp",
  "aud": "report-system-api",
  "exp": 1781777799
}
```

### 14.4 Multi-Branch Data Model

**New entity in every service that owns shared data:**

```sql
-- in EVERY service DB (auth_db, property_db, restaurant_db, ...)
CREATE TABLE branches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL,
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    name_kh         VARCHAR(255),
    branch_type     VARCHAR(50) NOT NULL,    -- HQ, WAREHOUSE, STORE, RESTAURANT, KIOSK
    parent_id       UUID REFERENCES branches(id),  -- hierarchy
    address         TEXT,
    city            VARCHAR(100),
    district        VARCHAR(100),
    province        VARCHAR(100),
    phone           VARCHAR(50),
    email           VARCHAR(255),
    timezone        VARCHAR(50) DEFAULT 'Asia/Phnom_Penh',
    locale          VARCHAR(10) DEFAULT 'km',
    currency        VARCHAR(3) DEFAULT 'USD',
    tax_rate        DECIMAL(5,2),
    logo_url        TEXT,
    settings        JSONB DEFAULT '{}',
    is_active       BOOLEAN DEFAULT true,
    is_default      BOOLEAN DEFAULT false,
    opened_at       DATE,
    closed_at       DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    UNIQUE(tenant_id, code)
);
CREATE INDEX idx_branches_tenant ON branches(tenant_id);
CREATE INDEX idx_branches_parent ON branches(parent_id);
CREATE INDEX idx_branches_tenant_active ON branches(tenant_id, is_active);
```

**Branch_id is added to every existing table** (sample, full list per service below):

| Service | Tables getting `branch_id` (NOT NULL, FK → branches) |
|---------|------------------------------------------------------|
| auth-service | users, refresh_tokens, user_roles, audit_logs |
| property-service | properties, units, leases, schedules, maintenance_tickets |
| restaurant-service | outlets, tables, menu_categories, menu_items, orders, customers, reservations |
| inventory-service | warehouses, products, stock_entries, stock_exits, purchase_orders |
| finance-service | accounts, journal_entries, invoices, employees, attendance_records, payroll_periods, tax_records |
| payment-service | payment_transactions, refunds, reconciliation_records, payment_gateway_configs |
| reporting-service | report_definitions, report_executions, scheduled_reports, dashboard_configs, aggregated_snapshots |

**Branch-aware queries:**
- Every list query gains `WHERE branch_id = :currentBranch` (or `branch_id = ANY(:branchIds)` for cross-branch reports)
- `branch_id` is taken from JWT claim, NOT request body (prevents IDOR)
- Cross-branch operations (reports, dashboards) use a separate `?scope=cross-branch` flag + elevated role

**User ↔ Branch mapping** (in auth_db):
```sql
CREATE TABLE user_branches (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    branch_id   UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    role        VARCHAR(50) NOT NULL,        -- per-branch role
    is_default  BOOLEAN DEFAULT false,
    created_at  TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (user_id, branch_id)
);
```

### 14.5 Cross-Branch Transfers

**Inventory transfer** (inventory-service + finance-service):
- New entity `stock_transfers`:
  - `from_branch_id`, `to_branch_id`, `status` (DRAFT/PENDING/SHIPPED/RECEIVED/CANCELLED)
  - Items with `product_id`, `quantity`, `unit_cost`
  - Approval workflow: requires manager role in source branch
- Two-sided journal entry: DR `Inventory (to)` / CR `Inventory (from)` + `Inter-Branch Clearing`
- Events: `stock.transfer.requested`, `stock.transfer.shipped`, `stock.transfer.received`, `stock.transfer.cancelled`
- UI: `/app/inventory/transfers` page

**Inter-branch financial transaction** (finance-service):
- `JournalEntry` gets `from_branch_id` (nullable) and `to_branch_id` (nullable) for inter-branch postings
- New `InterBranchClearing` account per tenant (auto-created)
- Consolidation report: `/api/finance/reports/inter-branch-clearing`

**Cross-branch view permissions:**
- Role `CROSS_BRANCH_VIEWER` allows seeing data from all branches
- Role `CROSS_BRANCH_MANAGER` allows transferring, posting inter-branch entries

### 14.6 Frontend Integration

**Keycloak JS adapter** (`@keycloak/keycloak-js`):
```typescript
// app/plugins/keycloak.client.ts
import Keycloak from '@keycloak/keycloak-js'

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const keycloak = new Keycloak({
    url: config.public.keycloakUrl,
    realm: config.public.tenantSlug,   // per-tenant realm
    clientId: 'report-system-web',
  })
  return { provide: { keycloak } }
})
```

**Branch selector in top nav** (`app/layouts/app.vue`):
- Dropdown of user's accessible branches
- Selection stored in Pinia + `localStorage`
- All `$api` calls append `?branchId=X` (read on server, validated against JWT)

**Login flow change:**
- Old: form POST → custom JWT
- New: redirect to Keycloak → PKCE → redirect back with code → backend exchanges for tokens
- `useAuth` composable wraps `keycloak.login()`, `keycloak.logout()`, `keycloak.updateToken()`

### 14.7 Updated Database Schemas (per service)

**Add to every Flyway V2 migration** (one per service):

```sql
-- V2__add_branches_and_branch_id.sql (auth_db)
CREATE TABLE branches ( ...as above... );
ALTER TABLE users     ADD COLUMN default_branch_id UUID REFERENCES branches(id);
ALTER TABLE users     ADD COLUMN branch_scope VARCHAR(20) DEFAULT 'single'  -- single|multi|all
                     CHECK (branch_scope IN ('single','multi','all'));
CREATE TABLE user_branches ( ...as above... );
CREATE INDEX idx_user_branches_user ON user_branches(user_id);
CREATE INDEX idx_user_branches_branch ON user_branches(branch_id);

-- Seed: HQ branch for existing demo tenant
INSERT INTO branches (id, tenant_id, code, name, branch_type, is_default, is_active, opened_at)
VALUES ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001',
        'HQ', 'Headquarters', 'HQ', true, true, CURRENT_DATE);
UPDATE users SET default_branch_id = '00000000-0000-0000-0000-000000000010'
 WHERE tenant_id = '00000000-0000-0000-0000-000000000001';
INSERT INTO user_branches (user_id, branch_id, role, is_default)
SELECT id, '00000000-0000-0000-0000-000000000010', 'BRANCH_MANAGER', true
  FROM users WHERE tenant_id = '00000000-0000-0000-0000-000000000001';
```

**Property, restaurant, inventory, finance, payment, reporting services:**
```sql
-- V2__add_branch_id.sql (per service DB)
CREATE TABLE branches ( ...as above... );
-- Insert HQ branch for existing demo tenant (idempotent INSERT)
INSERT INTO branches (id, tenant_id, code, name, branch_type, is_default, is_active, opened_at)
VALUES ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001',
        'HQ', 'Headquarters', 'HQ', true, true, CURRENT_DATE)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- Add branch_id column to every domain table
ALTER TABLE properties       ADD COLUMN branch_id UUID REFERENCES branches(id);
-- ... repeat for all tables listed in 14.4 ...

-- Backfill: set branch_id = HQ for all existing rows
UPDATE properties      SET branch_id = '00000000-0000-0000-0000-000000000010' WHERE branch_id IS NULL;
-- ... repeat ...

-- Make branch_id NOT NULL after backfill
ALTER TABLE properties       ALTER COLUMN branch_id SET NOT NULL;
-- ... repeat ...

-- Update unique constraints to be per-branch
ALTER TABLE properties DROP CONSTRAINT IF EXISTS properties_pkey;  -- (re-add if needed)
ALTER TABLE properties ADD CONSTRAINT properties_tenant_code_branch UNIQUE(tenant_id, code, branch_id);
-- ... repeat ...
```

### 14.8 New Kafka Topics

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `branch.created` | auth-service | all services (cache invalidation) | `{branchId, tenantId, code, name}` |
| `branch.updated` | auth-service | all services | `{branchId, tenantId, fields[]}` |
| `branch.deactivated` | auth-service | all services | `{branchId, tenantId}` |
| `stock.transfer.requested` | inventory-service | source + target branches (notifications) | `{transferId, fromBranchId, toBranchId, items[]}` |
| `stock.transfer.shipped` | inventory-service | target branch | `{transferId, shippedAt, carrier}` |
| `stock.transfer.received` | inventory-service | source + target (close loop) | `{transferId, receivedAt, discrepancies[]}` |
| `stock.transfer.cancelled` | inventory-service | both branches | `{transferId, reason}` |
| `auth.realm.created` | auth-service | all services (cache config) | `{tenantId, realm, keycloakUrl}` |

### 14.9 New Service Responsibilities

**auth-service** (significantly refactored):
- ❌ Remove: password hashing, JWT issuance, refresh tokens
- ✅ Keep: tenants CRUD, branches CRUD, users CRUD (sync with Keycloak), roles definitions
- ✅ Add: Keycloak Admin API client (create realm, create user, assign roles, federate identities)
- ✅ Add: branch CRUD, user-branch-role assignment
- ✅ Add: OIDC public-key cache (one per realm) for offline token validation
- Database: keeps `tenants`, `users`, `branches`, `user_branches`, `roles`, `permissions`; **drops** `refresh_tokens` (Keycloak manages)

**gateway**:
- Replace custom `JwtAuthGatewayFilter` with `KeycloakRealmResolver` + `OidcTokenValidator`
- New filter: `BranchContextFilter` — extracts branch from JWT or query param, validates against allowed branches
- Add per-realm JWK cache (one entry per active tenant)

**frontend**:
- Add `keycloak-js` package
- New plugin: `plugins/keycloak.client.ts`
- New layout: `app/layouts/branch.vue` for branch-scoped pages
- New Pinia store: `stores/branch.ts`
- Branch selector component: `app/components/BranchSelector.vue`
- Update middleware: `middleware/auth.ts` checks Keycloak session
- All `$api` calls add `?branchId=X` from store

### 14.10 Updated Service Port Map (unchanged ports, new services)

| Service | Port | Notes |
|---------|------|-------|
| auth-service | 8081 | Refactored: Keycloak federation |
| property-service | 8082 | + branch_id, + branch CRUD |
| restaurant-service | 8083 | + branch_id, + branch CRUD |
| inventory-service | 8084 | + branch_id, + branch CRUD, + stock_transfers |
| finance-service | 8085 | + branch_id, + branch CRUD, + inter-branch journal entries |
| payment-service | 8086 | + branch_id, + branch CRUD |
| reporting-service | 8087 | + branch_id, + branch CRUD, + cross-branch reports |
| gateway | 8080 | OIDC validation |
| eureka | 8761 | unchanged |
| nuxt-web | 3000 | + Keycloak adapter, branch selector |
| **keycloak** | **8180** | **NEW** — identity provider |
| nuxt-web | 3000 | unchanged |
| postgres | 5432 | + keycloak_db |
| kafka | 9092 | + new topics |
| redis | 6379 | + JWK cache |

### 14.11 Updated Docker Compose (services added)

```yaml
# docker-compose.yml (excerpt — additions)
services:
  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    command: start --import-realm
    container_name: report-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD:-admin}
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres-keycloak:5432/keycloak_db
      KC_DB_USERNAME: report_user
      KC_DB_PASSWORD: report_pass
    ports:
      - "8180:8080"
    volumes:
      - ./docker/keycloak/realm-export.json:/opt/keycloak/data/import/master-realm.json
    depends_on:
      postgres-keycloak:
        condition: service_healthy

  postgres-keycloak:
    image: postgres:16-alpine
    container_name: report-postgres-keycloak
    environment:
      POSTGRES_USER: report_user
      POSTGRES_PASSWORD: report_pass
      POSTGRES_DB: keycloak_db
    volumes:
      - report-keycloak-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U report_user -d keycloak_db"]
      interval: 10s
      timeout: 5s
      retries: 5
```

### 14.12 Migration Strategy (zero-downtime)

**Phase 1: Add columns, keep old auth running (Sprint 9)**
1. Add `branches` table + `branch_id` columns (nullable) to all 7 service DBs
2. Backfill `branch_id = HQ` for all existing rows
3. Deploy code that READS from new columns but WRITES to both old (tenant_id only) and new (tenant_id + branch_id)
4. Old JWTs still work, no client changes

**Phase 2: Cut over to Keycloak (Sprint 10 part A)**
1. Stand up Keycloak with master realm
2. Migrate existing users from auth-service to Keycloak (one-time script: hash → reset password → Keycloak user)
3. Switch gateway to validate Keycloak JWTs (dual-validate both during transition)
4. Old `/api/auth/login` endpoint returns 301 → Keycloak login page
5. Frontend redirects to Keycloak on first unauthenticated visit
6. Monitor for 1 week, decommission auth-service password endpoints

**Phase 3: Add branch enforcement (Sprint 10 part B)**
1. Make `branch_id` NOT NULL in all tables
2. All list endpoints require branch context (return 400 if missing)
3. Frontend branch selector becomes required UI element
4. Update all 27 frontend pages to filter by branch

**Phase 4: Cross-branch features (Sprint 11)**
1. Stock transfers end-to-end
2. Inter-branch journal entries
3. Cross-branch reports/dashboards
4. Bulk operations (close month for all branches in one click)

### 14.13 Sprint 9 — Keycloak Foundation (Weeks 17–18)

**Deliverables:**
1. **Keycloak container** in docker-compose + initial realm-export.json (master realm only)
2. **Keycloak Admin API client** in auth-service (Java SDK or REST wrapper)
3. **Realm creation** endpoint: `POST /api/admin/realms` (super-admin only) creates a new tenant + Keycloak realm in one transaction
4. **User migration script**: `auth-service` → Keycloak (one-time)
5. **Gateway OIDC filter**: validate Keycloak JWTs using realm-specific JWK
6. **Frontend keycloak-js plugin**: PKCE login, token refresh, logout
7. **Existing users migrated**: `admin@demo.com` works through Keycloak
8. **Backward compat**: `/api/auth/login` still works (returns deprecation warning header)

**Files:**
```
docker/keycloak/realm-export.json                  NEW
docker/keycloak/themes/report-system/              NEW (custom login theme)
services/auth-service/.../keycloak/KeycloakAdminClient.java   NEW
services/auth-service/.../web/AdminRealmController.java        NEW
infrastructure/gateway/.../filter/OidcTokenValidator.java      NEW (replaces JwtAuthGatewayFilter)
infrastructure/gateway/.../filter/KeycloakJwksCache.java       NEW
frontend/.../app/plugins/keycloak.client.ts                    NEW
frontend/.../app/composables/useKeycloak.ts                    NEW
frontend/.../app/composables/useAuth.ts                        MODIFIED (delegates to Keycloak)
docker-compose.yml                                            MODIFIED
```

### 14.14 Sprint 10 — Multi-Branch Foundation (Weeks 19–20)

**Deliverables:**
1. **`branches` table** in all 7 service DBs (V2 migration)
2. **`branch_id` columns** on all 30+ domain tables, backfilled with HQ
3. **Branch CRUD** in every service (`GET/POST/PUT/DELETE /branches`)
4. **User-branch-role** mapping in auth-service
5. **JWT claims** updated: includes `branch_ids[]`, `current_branch`, `branch_roles{}`
6. **Branch context filter** in gateway: validates requested branch against JWT
7. **Branch selector component** in frontend
8. **All list endpoints** require `?branchId=X` (or use JWT default)
9. **27 frontend pages** updated to use branch filter
10. **Branch admin UI** in `/app/admin/branches` (list, create, edit, assign users)

**Files:**
```
db/migration/V2__add_branches_and_branch_id.sql     NEW (×7 services)
domain/model/Branch.java                            NEW (×7 services)
domain/port/inbound/BranchUseCase.java              NEW (×7 services)
domain/port/outbound/BranchRepository.java          NEW (×7 services)
domain/service/BranchServiceImpl.java               NEW (×7 services)
infrastructure/persistence/entity/BranchEntity.java NEW (×7 services)
infrastructure/persistence/repository/JpaBranchRepository.java NEW
infrastructure/persistence/adapter/JpaBranchAdapter.java       NEW
infrastructure/web/BranchController.java            NEW (×7 services)
infrastructure/event/BranchEventPublisher.java      NEW (auth-service)
infrastructure/event/BranchEventConsumer.java       NEW (×6 other services)
shared/common-dto/.../event/BranchEvent.java        NEW
frontend/.../app/components/BranchSelector.vue      NEW
frontend/.../app/pages/app/admin/branches/         NEW (4 pages)
frontend/.../app/stores/branch.ts                   NEW
frontend/.../app/layouts/app.vue                    MODIFIED (add selector)
```

### 14.15 Sprint 11 — Cross-Branch Features (Weeks 21–22)

**Deliverables:**
1. **Stock transfer workflow**:
   - `stock_transfers` table, items, statuses
   - `POST /api/inventory/transfers` (create draft)
   - `POST /api/inventory/transfers/{id}/ship` (decrement source stock)
   - `POST /api/inventory/transfers/{id}/receive` (increment target stock, flag discrepancies)
   - Kafka events: `stock.transfer.*`
   - UI: `/app/inventory/transfers` (list, create, receive, cancel)
2. **Inter-branch journal entries**:
   - `JournalEntry.from_branch_id`, `to_branch_id`
   - `InterBranchClearing` account auto-created per tenant
   - Auto-posted when transfer is received
3. **Cross-branch reports**:
   - New `ReportScope` enum: SINGLE_BRANCH, MULTI_BRANCH, ALL_BRANCHES
   - Reports can be configured to aggregate across selected branches
   - Dashboard widgets can show side-by-side branch comparisons
4. **Bulk operations**:
   - "Close month for all branches" button (admin only)
   - Bulk branch creation (CSV import)
   - Bulk user-branch assignment
5. **Branch hierarchy UI** (basic, no parent/child enforcement yet)
6. **Transfer approval workflow** (manager role in source branch required)
7. **Audit trail for cross-branch ops** (Kafka events → reporting service)

**Files:**
```
db/migration/V3__add_stock_transfers.sql            NEW (inventory_db)
db/migration/V3__add_inter_branch_journal.sql      NEW (finance_db)
domain/model/StockTransfer.java                     NEW (inventory-service)
domain/service/StockTransferServiceImpl.java        NEW
infrastructure/web/StockTransferController.java     NEW
infrastructure/event/StockTransferEventPublisher.java NEW
shared/common-dto/.../event/StockTransferEvent.java  NEW
domain/model/InterBranchClearingAccount.java        NEW (finance-service)
infrastructure/web/InterBranchTransferController.java NEW
frontend/.../app/pages/app/inventory/transfers/    NEW (3 pages)
frontend/.../app/pages/app/admin/transfers/        NEW (2 pages)
frontend/.../app/components/InterBranchTransferForm.vue NEW
```

### 14.16 Updated File Structure (additions only)

```
Report_System/
├── docker/
│   ├── keycloak/                                NEW
│   │   ├── realm-export.json                    NEW — master realm
│   │   ├── realm-demo-corp.json                 NEW — template
│   │   ├── themes/
│   │   │   └── report-system/                   NEW — custom login theme
│   │   └── Dockerfile                           NEW
│   └── scripts/
│       ├── migrate-users-to-keycloak.sh         NEW
│       ├── seed-sample-data.sh                  EXISTING (updated with branch_id)
│       └── seed-branches.sh                     NEW
│
├── services/auth-service/src/main/java/.../
│   ├── domain/model/
│   │   ├── Branch.java                          NEW
│   │   ├── UserBranch.java                      NEW
│   ├── domain/port/
│   │   ├── inbound/BranchUseCase.java           NEW
│   │   ├── outbound/BranchRepository.java       NEW
│   │   ├── outbound/UserBranchRepository.java   NEW
│   ├── domain/service/
│   │   ├── BranchServiceImpl.java               NEW
│   │   ├── UserBranchServiceImpl.java           NEW
│   ├── infrastructure/
│   │   ├── keycloak/
│   │   │   ├── KeycloakAdminClient.java         NEW
│   │   │   ├── KeycloakRealmManager.java        NEW
│   │   │   ├── KeycloakUserSync.java            NEW
│   │   │   └── KeycloakJwksCache.java           NEW
│   │   ├── web/
│   │   │   ├── BranchController.java            NEW
│   │   │   ├── UserBranchController.java        NEW
│   │   │   ├── AdminRealmController.java        NEW
│   │   ├── event/
│   │   │   ├── BranchEventPublisher.java        NEW
│   │   │   ├── RealmEventPublisher.java         NEW
│   ├── resources/db/migration/
│   │   ├── V1__init.sql                         EXISTING
│   │   ├── V2__add_branches_and_branch_id.sql   NEW
│   │   ├── V3__add_user_branches.sql            NEW
│
├── services/{property,restaurant,inventory,finance,payment,reporting}-service/
│   ├── ... (mirror of auth-service branch files)
│   └── resources/db/migration/
│       ├── V2__add_branches_and_branch_id.sql   NEW
│
├── infrastructure/gateway/src/main/java/.../filter/
│   ├── JwtAuthGatewayFilter.java                EXISTING (replaced)
│   ├── OidcTokenValidator.java                  NEW
│   ├── KeycloakJwksCache.java                   NEW
│   ├── BranchContextFilter.java                 NEW
│   ├── TenantContextFilter.java                 MODIFIED (read from JWT)
│
├── frontend/report-system-web/
│   ├── app/
│   │   ├── components/
│   │   │   ├── BranchSelector.vue               NEW
│   │   │   ├── InterBranchTransferForm.vue      NEW
│   │   ├── composables/
│   │   │   ├── useKeycloak.ts                   NEW
│   │   │   ├── useAuth.ts                       MODIFIED
│   │   ├── plugins/
│   │   │   ├── keycloak.client.ts               NEW
│   │   ├── stores/
│   │   │   ├── branch.ts                        NEW
│   │   ├── middleware/
│   │   │   ├── auth.ts                          MODIFIED
│   │   ├── layouts/
│   │   │   ├── app.vue                          MODIFIED (branch selector)
│   │   ├── pages/app/
│   │   │   ├── admin/branches/                  NEW (4 pages)
│   │   │   ├── admin/transfers/                 NEW (2 pages)
│   │   │   ├── inventory/transfers/             NEW (3 pages)
│   │   │   ├── {all 27 existing pages}          MODIFIED (add branch filter)
│   ├── package.json                             MODIFIED (+@keycloak/keycloak-js)
```

### 14.17 Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Keycloak adds latency to every request | Med | Med | JWK cache (5min TTL), in-memory, one fetch per realm per 5min |
| Migration of existing users breaks passwords | High | High | Force password reset on first Keycloak login; never auto-migrate hashes |
| Branch_id backfill fails for large tables | Med | Med | Run in batches of 10k rows, monitor replication lag |
| One realm per tenant → too many realms (1000+) | Med | Med | Use shared realm if customer count > 500, with client-per-tenant (revisit in 6 months) |
| Cross-branch reports slow for 100+ branches | Med | Med | Materialized views + scheduled aggregations, not on-the-fly joins |
| Keycloak upgrade breaks auth | Low | Critical | Pin version (24.0.x), test in staging, keep custom theme in version control |
| User has 200 branches, JWT too large | Low | Low | Compress JWT, or use opaque tokens with introspection endpoint |

### 14.18 Acceptance Criteria

**Sprint 9 done when:**
- [ ] Keycloak running in docker-compose
- [ ] Creating a tenant via `POST /api/admin/realms` creates both auth-service tenant record AND Keycloak realm in <5s
- [ ] Login via Keycloak (browser redirect flow) works end-to-end
- [ ] Login via existing password endpoint still works (with deprecation header)
- [ ] All 7 services accept Keycloak JWTs
- [ ] Frontend login page redirects to Keycloak

**Sprint 10 done when:**
- [ ] All 30+ tables have `branch_id` (NOT NULL) with HQ branch seeded
- [ ] Branch CRUD works in all 7 services
- [ ] User can switch active branch in UI; all subsequent API calls respect the choice
- [ ] User without access to a branch gets 403 on requests to that branch
- [ ] 27 frontend pages filter by current branch
- [ ] Branch admin UI allows assigning users to branches with roles

**Sprint 11 done when:**
- [ ] Can create a stock transfer from Branch A to Branch B
- [ ] Source stock decrements on ship, target stock increments on receive
- [ ] Transfer creates matching inter-branch journal entries
- [ ] Cross-branch report aggregates data from selected branches
- [ ] Bulk "close month" operation works for all branches
- [ ] All operations have audit trail events

### 14.19 Out of Scope (deferred)

- Branch hierarchy (parent/child enforcement) — flat for now
- Branch-level billing (per-branch subscription) — tenant-level only
- Branch-scoped SSO config (per-branch IdP) — realm-level only
- Branch transfer approval workflow with multiple approvers — single approver for MVP
- Branch-level customization (themes, workflows) — global only
- Real-time branch sync (CDC, event sourcing) — eventual consistency via Kafka
- Keycloak cluster mode (HA) — single instance for MVP

---

## 15. Updated Infrastructure Stack

```yaml
# Final docker-compose.yml service list (after Sprint 11)
services: 18 total
  # Application
  - eureka, gateway, nuxt-web
  - auth-service, property-service, restaurant-service, inventory-service
  - finance-service, payment-service, reporting-service

  # Identity
  - keycloak                        NEW

  # Data
  - postgres, postgres-keycloak     NEW
  - kafka, kafka-zookeeper
  - redis, minio, zipkin
```

## 16. Updated Team Estimate (sprints 9–11 add 6 weeks)

| Sprint | Weeks | Engineers | Deliverable |
|--------|-------|-----------|-------------|
| 9 | 17–18 | 2 BE + 1 FE | Keycloak SSO foundation |
| 10 | 19–20 | 2 BE + 1 FE | Multi-branch foundation |
| 11 | 21–22 | 2 BE + 1 FE | Cross-branch features |
| 12 (new) | 23–24 | 2 BE + 1 FE | E2E tests, migration tools, docs |
| **Total** | **8 months** | 2–3 | MVP + branches + SSO |

---

*End of plan. See `AGENTS.md` for current implementation status.*

