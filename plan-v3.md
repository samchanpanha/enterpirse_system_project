# plan-v3.md — Complete System Build Plan

> **Goal**: Production-ready enterprise SaaS with 7 + 2 new microservices, 70+ frontend pages, real-time POS, delivery tracking, real estate marketplace, housing management — all multi-tenant, multi-branch, with Khmer/English i18n.

---

## 0. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Gateway (Port 8080)                      │
│  RouteConfig (16 routes) + OidcTokenValidator + BranchFilter │
├─────────────────────────────────────────────────────────────┤
│                     Eureka (Port 8761)                         │
├──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┤
│ Auth │Prop  │Rest  │Inven │Finan │Paymt │Rprt  │*Deliv│*Real │
│8081  │8082  │8083  │8084  │8085  │8086  │8087  │8088  │8089  │
├──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┤
│                         PostgreSQL × 9                        │
│                Kafka (8 topics) + Redis + Minio               │
└─────────────────────────────────────────────────────────────┘
```

**Frontend**: Angular 19.2 + PrimeNG 19.1.4 + Tailwind CSS 4.3 + Keycloak + Chart.js + Leaflet

---

## 1. Existing Backend Summary

| Service | Port | Controllers | Entities | Endpoints | Status |
|---------|------|-------------|----------|-----------|--------|
| Auth | 8081 | 9 | 11 | 30+ | ✅ Complete |
| Property | 8082 | 5 | 7 | 20+ | ✅ Complete |
| Restaurant | 8083 | 5 | 8 | 18+ | ✅ Needs POS endpoints |
| Inventory | 8084 | 5 | 11 | 22+ | ✅ Complete |
| Finance | 8085 | 6 | 12 | 18+ | ✅ Complete |
| Payment | 8086 | 1 | 4 | 7 | ✅ Needs gateway config CRUD |
| Reporting | 8087 | 2 | 5 | 10 | ✅ Complete |

### Key Gaps
- **Restaurant**: No POS cart/split/discount/barcode/payment endpoints
- **Payment**: No PaymentGatewayConfig CRUD endpoint
- **Missing services**: Delivery (port 8088), Realty (port 8089)

---

## 2. New Backend Services

### 2.1 Delivery Service (8088) — NOT BUILT

**Database**: `delivery_db`

**Entities**: Delivery, Driver, FleetVehicle, DeliveryZone, DeliveryProof, DriverPayout

**Controllers** (5):
- `DeliveryController` — POST `/`, GET `/{id}`, GET `/by-tenant/{tenantId}`, PUT `/{id}/assign`, PUT `/{id}/status`, PUT `/{id}/location`, POST `/{id}/proof`, GET `/tracking/{id}`
- `DriverController` — POST `/`, GET `/{id}`, GET `/by-tenant/{tenantId}`, PUT `/{id}`, PUT `/{id}/location`, GET `/{id}/earnings`
- `FleetController` — POST `/`, GET `/{id}`, GET `/by-tenant/{tenantId}`, PUT `/{id}`
- `ZoneController` — POST `/`, GET `/{id}`, GET `/by-tenant/{tenantId}`, PUT `/{id}`
- `PayoutController` — GET `/drivers/{id}`, POST `/pay`, GET `/history`

**WebSocket**: `/ws/delivery` — driver position + delivery status

**Kafka**: Consumes `sale-events`, produces `delivery.created`, `delivery.status.changed`, `delivery.position`

**Flyway**: V1 (schema) + V2 (branches)

**Docker**: Port 8088, standard Dockerfile

**Gateway**: `/api/delivery/**` → `lb://delivery-service/deliveries/**`

### 2.2 Realty Service (8089) — NOT BUILT

**Database**: `realty_db`

**Entities** (12): Listing, ListingImage, Agent, Lead, Tour, Offer, Commission, PropertyValuation, HoaFee, Resident, AmenityBooking, Visitor, Package

**Controllers** (11):
- `ListingController` — POST `/{id}`, GET `/{id}`, PUT `/{id}`, PATCH `/{id}/status`, GET `/by-tenant/{tenantId}`, GET `/search`, POST `/{id}/images`, DELETE `/{id}/images/{imgId}`
- `AgentController` — POST `/`, GET `/{id}`, PUT `/{id}`, GET `/by-tenant/{tenantId}`
- `LeadController` — POST `/`, GET `/{id}`, PUT `/{id}`, PATCH `/{id}/status`, GET `/by-agent/{agentId}`, GET `/by-tenant/{tenantId}`
- `TourController` — POST `/`, GET `/{id}`, PATCH `/{id}/status`, GET `/by-agent/{agentId}/date`
- `OfferController` — POST `/`, GET `/{id}`, POST `/{id}/respond`, GET `/by-listing/{listingId}`
- `ValuationController` — GET `/{propertyId}`, POST `/recalculate`
- `HoaController` — POST `/fees`, GET `/fees/by-property/{propertyId}`, POST `/fees/{id}/pay`, GET `/by-tenant/{tenantId}`
- `ResidentController` — POST `/`, GET `/{id}`, PUT `/{id}`, GET `/by-property/{propertyId}`
- `AmenityController` — POST `/`, GET `/{id}`, GET `/by-date/{date}`, POST `/{id}/cancel`
- `VisitorController` — POST `/`, GET `/{id}`, PUT `/{id}/check-in`, PUT `/{id}/check-out`
- `PackageController` — POST `/`, GET `/{id}`, PUT `/{id}/pick-up`

**Kafka**: Consumes `payment.received` (auto-mark HOA fee paid), produces `realty.listing.changed`, `realty.lead.changed`, `realty.hoa.payment.received`

**Flyway**: V1 (schema) + V2 (branches) + V3 (customers)

**Docker**: Port 8089, standard Dockerfile

**Gateway**: `/api/realty/**` → `lb://realty-service/realty/**`

### 2.3 POS Enhancements to Restaurant Service

**New endpoints**:
- `POST /pos/{outletId}/cart` — create cart
- `POST /pos/{outletId}/cart/{id}/items` — add item
- `DELETE /pos/{outletId}/cart/{id}/items/{itemId}` — remove item
- `PUT /pos/{outletId}/cart/{id}/discount` — apply discount
- `POST /pos/{outletId}/cart/{id}/split` — split bill
- `POST /pos/{outletId}/cart/{id}/payment` — record payment
- `GET /pos/{outletId}/barcode/{code}` — barcode lookup
- `POST /pos/{outletId}/quick-sale` — quick cashier
- `GET /pos/{outletId}/x-report` — X-report
- `GET /pos/{outletId}/z-report` — Z-report
- `POST /pos/kds/{outletId}/bump/{orderItemId}` — KDS bump
- `POST /pos/kds/{outletId}/recall/{orderItemId}` — KDS recall
- `GET /pos/receipt/{orderId}` — receipt data

### 2.4 Payment Service Enhancement

**New endpoint + entity**:
- `PaymentGatewayConfigController` — CRUD for gateway configs
- `PaymentGatewayConfigEntity` — id, tenantId, gateway (ABA/WING/PI_PAY), config JSONB, isActive, isSandbox

**Migration**: V3__add_gateway_configs.sql

---

## 3. Existing Frontend Pages (27 pages, all ✅)

| Module | Pages | Status |
|--------|-------|--------|
| Auth | Login (enterprise split-screen, locale switcher, SSO, dark mode) | ✅ |
| Dashboard | 4 stat cards (needs real API data) | 🟡 |
| Property | List, Form, Detail, Units, UnitFormDialog, Leases, LeaseFormDialog | ✅ |
| Restaurant | OutletList, OutletDetail, MenuList, OrderList, CustomerList, ReservationList | ✅ |
| Inventory | ProductList, ProductForm, SupplierList, POList, PODetail, StockTransferList | ✅ |
| Finance | CoAList, AccountForm, JournalEntryList, InvoiceList, InvoiceDetail, InvoiceForm, TaxList, EmployeeList, PayrollList | ✅ |
| Payment | PaymentList | ✅ |
| Reporting | ReportList | ✅ |

## 4. Missing Frontend Pages (43 pages)

### Admin (7)
- `/admin/users` — User list + CRUD **P1**
- `/admin/users/:id` — User detail **P1**
- `/admin/tenants` — Tenant list + CRUD **P1**
- `/admin/tenants/new`, `/:id` — Tenant create wizard **P1**
- `/admin/features` — Feature flags **P2**
- `/admin/realms` — Realm admin panel **P2**
- `/admin/branches` — Branch hierarchy mgmt **P1**

### POS (7)
- `/pos/:outletId` — POS Terminal **P0**
- `/pos/:outletId/tables` — Table select **P0**
- `/pos/kds/:outletId` — KDS display **P1**
- `/pos/quick` — Quick cashier **P2**
- `/pos/reports/:outletId` — Daily reports **P2**
- `/kiosk/:outletId` — Self-service kiosk **P2**
- `/pos/settings` — Barcode/printer config **P2**

### Real Estate (9)
- `/realty` — Property listings **P0**
- `/realty/:id` — Listing detail **P0**
- `/realty/new`, `/:id/edit` — Listing form **P0**
- `/realty/agents` — Agent CRM **P1**
- `/realty/leads` — Lead pipeline (kanban) **P1**
- `/realty/offers` — Offer management **P2**
- `/realty/valuation` — Property valuation **P2**
- `/realty/mls` — MLS integration **P2**
- `/realty/search` — Search with map/filters **P1**

### Housing (7)
- `/realty/housing/residents` — Resident directory **P1**
- `/realty/housing/amenities` — Amenity booking **P1**
- `/realty/housing/dues` — HOA dues **P1**
- `/realty/housing/visitors` — Visitor management **P2**
- `/realty/housing/packages` — Package tracking **P2**
- `/realty/housing/maintenance` — Maintenance requests **P1**
- `/realty/housing/community` — Community board **P3**

### Delivery (5)
- `/delivery` — Dispatch board **P0**
- `/delivery/drivers` — Driver management **P1**
- `/delivery/fleet` — Fleet management **P2**
- `/delivery/tracking/:id` — Real-time map **P1**
- `/delivery/payouts` — Driver payouts **P2**

### Finance (4)
- `/finance/bank-recon` — Bank reconciliation **P2**
- `/finance/bills` — Bills (AP) **P2**
- `/finance/reports` — P&L, BS reports **P2**
- `/finance/invoices/:id` — Invoice detail (enhanced) **P1**

### Reporting (4)
- `/reports/:id/run` — Report runner **P2**
- `/reports/scheduled` — Scheduled reports **P2**
- `/reports/history` — Execution history **P2**
- `/reports/cross-branch` — Cross-branch compare **P3**

### Cross-cutting (4)
- `/notifications` — Notification center **P2**
- `/profile` — Profile/preferences **P1**
- `/admin/activity` — Audit log **P3**
- `/help` — Docs **P3**

---

## 5. Database Schema

### Current (7 databases, 24 Flyway migrations)

```
auth_db:      tenants, users, branches, user_branches, roles, permissions,
              role_permissions, user_roles, features, client_features, refresh_tokens

property_db:  properties, units, leases, schedules, maintenance_tickets,
              branches, user_branches, customers

restaurant_db: outlets, tables, categories, menu_items, customers,
               orders, order_items, reservations, branches, user_branches

inventory_db: products, product_categories, suppliers, warehouses,
              stock_entries, stock_exits, purchase_orders, purchase_order_items,
              stock_transfers, stock_transfer_items, branches, user_branches, customers

finance_db:   chart_of_accounts, journal_entries, journal_entry_lines,
              invoices, invoice_items, tax_records, tax_filing_reports,
              employees, attendance_records, payroll_periods, payroll_items,
              branches, user_branches, customers

payment_db:   payment_transactions, gateway_logs, refunds, reconciliation_records,
              branches, user_branches

reporting_db: report_definitions, report_executions, scheduled_reports,
              dashboard_configs, aggregated_snapshots, branches, user_branches
```

### New (2 databases)

```
delivery_db:  deliveries, drivers, fleet_vehicles, delivery_zones,
              delivery_proofs, driver_payouts, branches, user_branches, customers

realty_db:    listings, listing_images, agents, leads, tours, offers,
              commissions, property_valuations, hoa_fees, residents,
              amenity_bookings, visitors, packages,
              branches, user_branches, customers
```

### Cross-cutting patterns
- `id UUID NOT NULL DEFAULT gen_random_uuid()`
- `tenant_id UUID NOT NULL`
- `branch_id UUID NOT NULL`
- `created_at TIMESTAMP DEFAULT now()`, `updated_at TIMESTAMP`
- jsonb columns for flexible data
- Identical `branches` + `user_branches` tables per service (V2 migration)

---

## 6. Kafka Topics

| Topic | Producer | Consumer(s) | Purpose |
|-------|----------|-------------|---------|
| `sale-events` | Restaurant | Inventory (auto-deduct), Finance (auto-invoice), Delivery (auto-dispatch) | Real-time sale processing |
| `rent-invoice-events` | Property | Finance (auto-JE) | Rent invoice generation |
| `stock.received` | Inventory | Finance (inventory JE) | Stock in |
| `stock.low` | Inventory | Notifications | Low stock alerts |
| `stock.transfer.*` | Inventory | Finance (inter-branch JE) | Multi-branch transfers |
| `payment.received` | Payment | Finance (auto-apply), Realty (HOA auto-pay) | Payment processing |
| `payment.failed` | Payment | Notifications | Payment failure |
| `delivery.*` (NEW) | Delivery | Notifications (SMS/push) | Delivery status |
| `realty.*` (NEW) | Realty | Notifications (email) | Listing/lead changes |

---

## 7. Gateway Routes to Add

In `RouteConfig.java`:
```java
.route(r -> r.path("/api/delivery/**")
    .filters(f -> f.stripPrefix(2))
    .uri("lb://delivery-service"))

.route(r -> r.path("/api/realty/**")
    .filters(f -> f.stripPrefix(2))
    .uri("lb://realty-service"))

.route(r -> r.path("/api/pos/**")
    .filters(f -> f.stripPrefix(2))
    .uri("lb://restaurant-service"))
```

---

## 8. Shared Frontend Components to Build First

| Component | Inputs | Priority |
|-----------|--------|----------|
| `EmptyState` | icon, title, description, actionLabel, (action) | P0 |
| `SkeletonTable` | rows, cols | P0 |
| `ConfirmDialog` | title, message, confirmText, destructive, (confirm) | P0 |
| `PageHeader` | title, breadcrumbs, action slot | P0 |
| `SearchBar` | (search) | P0 |
| `DataTable` | rows, columns, sortable, filterable, selectable | P1 |
| `FormField` | type, label, options, validators | P1 |

### Services/Composables
| Service | Purpose | Priority |
|---------|---------|----------|
| `NotifyService` | Standardized toasts | P0 |
| `PaginationService` | Server-side pagination state | P0 |
| `ApiErrorHandler` | Standardized error handling | P0 |
| `WebSocketService` | Auto-reconnect WebSocket | P1 |
| `LocaleService` | Khmer/English i18n | P1 |
| `OfflineQueueService` | IndexedDB mutation queue | P2 |

---

## 9. New npm Dependencies

```json
{
  "leaflet": "^1.9.4",
  "@types/leaflet": "^1.9.14",
  "html5-qrcode": "^2.3.8",
  "ng2-charts": "^5.0.4",
  "chart.js": "^4.4.7",
  "idb": "^8.0.0",
  "@angular/pwa": "^19.2.0"
}
```

---

## 10. Phased Execution Plan

### Phase 0: Foundation (Week 1, 5 days)
**Backend**:
- [ ] Cart/POS endpoints → restaurant-service (PosController, PosService, PosCart entity)
- [ ] Barcode lookup → inventory-service (add barcode scan endpoint)
- [ ] Gateway config CRUD → payment-service
- [ ] Delivery service scaffold (pom.xml, Dockerfile, V1 migration, basic entity wiring)
- [ ] Realty service scaffold (pom.xml, Dockerfile, V1 migration, basic entity wiring)
- [ ] Gateway route updates (delivery, realty, pos)
- [ ] `docker-compose.yml` updates (new services, new DBs)
- [ ] `init-dbs.sql` updates (delivery_db, realty_db)

**Frontend**:
- [ ] Shared components: EmptyState, SkeletonTable, ConfirmDialog, PageHeader, SearchBar
- [ ] Services: NotifyService, PaginationService, ApiErrorHandler
- [ ] Dashboard: wire to real API data (property, inventory, finance counts)
- [ ] Admin: UserManagement page, TenantManagement page, BranchManagement page
- [ ] Profile/Preferences page

**Verification**: `mvn compile`, `npm run build`, 22/22 e2e

### Phase 1: POS Terminal (Week 2, 5 days)
**Backend**:
- [ ] Complete POS endpoints (cart create/add/remove, discount, split, payment)
- [ ] KDS bump/recall endpoints
- [ ] X-report/Z-report aggregation
- [ ] Quick sale endpoint

**Frontend**:
- [ ] POS Terminal page (item grid, cart sidebar, payment panel, method split)
- [ ] KDS Display page (WebSocket tickets, bump bar, recall, timer)
- [ ] Table Select page (floor plan grid)
- [ ] Quick Cashier mode (no-login minimal POS)
- [ ] Daily Sales Report page (X-report, Z-report with Chart.js)

**Verification**: End-to-end POS flow (select table → add items → split bill → take payment → KDS bump)

### Phase 2: Real Estate Backend (Week 2-3, 5 days)
- [ ] All 12 domain models, ports, service implementations
- [ ] All 11 controllers with full CRUD + search
- [ ] Flyway V1 (all tables), V2 (branches), V3 (customers)
- [ ] Kafka integration (realty topics)
- [ ] Seed data script additions

**Verification**: Create listing → search → submit lead → schedule tour → make offer

### Phase 3: Real Estate Frontend (Week 3, 5 days)
- [ ] Listings page (photo grid, price, badges, featured flag)
- [ ] Listing detail (carousel, map, booking, contact agent, lead form)
- [ ] Listing create/edit (multi-step: details, photos, location, pricing)
- [ ] Agent CRM page (profile, listings, sales stats)
- [ ] Lead pipeline kanban board (drag-drop status transitions)
- [ ] Offer management page
- [ ] Property valuation viewer

### Phase 4: Housing + Delivery Backend (Week 4, 5 days)
- [ ] Housing entities (resident, amenity, HOA fee, visitor, package)
- [ ] HOA fee auto-generation (cron/scheduled task)
- [ ] Visitor QR code generation
- [ ] Package tracking workflow
- [ ] Full delivery-service implementation (all entities, services, controllers)
- [ ] Driver GPS WebSocket
- [ ] Zone-based pricing engine
- [ ] Proof of delivery upload (MinIO presigned URLs)
- [ ] Driver payout calculation

### Phase 5: Housing + Delivery Frontend (Week 4-5, 5 days)
- [ ] Housing: Resident directory, amenity booking calendar, HOA dues page
- [ ] Housing: Visitor management (QR pass), package tracking
- [ ] Housing: Maintenance requests (tenant submit + management view)
- [ ] Housing: Community board (announcements, polling)
- [ ] Delivery: Dispatch board (real-time orders, assign driver dropdown)
- [ ] Delivery: Driver management page
- [ ] Delivery: Fleet management page
- [ ] Delivery: Real-time tracking map (Leaflet, WebSocket positions)
- [ ] Delivery: Driver payout page

### Phase 6: Reports + Finance Deepen (Week 5, 5 days)
- [ ] Bank reconciliation page
- [ ] P&L / Balance Sheet report viewer (Chart.js)
- [ ] Report runner (parameter form + execute + download)
- [ ] Scheduled reports management (cron editor)
- [ ] Report execution history page
- [ ] Cross-branch report comparison
- [ ] Invoice detail: print, email, PDF download
- [ ] Bills (AP) page

### Phase 7: Cross-Cutting UX (Week 6, 5 days)
- [ ] Notification center (bell icon, real-time WebSocket, mark read)
- [ ] Activity/audit log page
- [ ] Help/docs page (markdown-rendered, bilingual)
- [ ] Khmer i18n: translate ALL pages (existing + new)
- [ ] Dark mode: verify all pages, fix gaps
- [ ] Mobile responsive: test 375px, 768px, fix issues
- [ ] SkeletonTable on all list pages
- [ ] EmptyState on all list pages
- [ ] ConfirmDialog on all destructive actions
- [ ] Charts on Dashboard (revenue trend, top products, AR aging)

### Phase 8: Offline + PWA (Week 6-7, 3 days)
- [ ] Service worker (`@angular/pwa`)
- [ ] IndexedDB queue for offline POS mutations
- [ ] Offline banner component
- [ ] Sync indicator + conflict resolution
- [ ] Push notifications (new orders, delivery assignments)
- [ ] Backend sync endpoint for offline queue replay

### Phase 9: Launch Prep (Week 7, 4 days)
- [ ] Landing page (marketing hero, features, pricing CTA)
- [ ] Pricing page (3 tiers: Starter $29, Growth $79, Enterprise $199)
- [ ] Changelog page (auto-generated from git tags)
- [ ] Email templates: invite, receipt, payslip, password reset
- [ ] Demo seed script (1 fresh tenant + 30 days data)
- [ ] Prometheus + Grafana for new services
- [ ] Production docker-compose.prod.yml updates
- [ ] k8s manifests for new services
- [ ] Full e2e smoke test (35+ checks)
- [ ] Lighthouse audit (mobile > 80)
- [ ] axe-core accessibility audit (0 critical)

---

## 11. Revenue Model

| Tier | Price | Modules | Target Market |
|------|-------|---------|---------------|
| **Starter** | $29/mo | Auth, Basic Reports, 1 branch, 3 users | Small business |
| **Growth** | $79/mo | +Property, +Restaurant POS, +Inventory, 5 branches | Mid-market |
| **Enterprise** | $199/mo | All modules + Delivery + Realty + Housing, unlimited | Enterprise |
| **POS Lite** | $9/mo/outlet | Standalone POS, offline mode, basic reporting | F&B stalls |
| **Realty** | $49/mo/agent | MLS integration, commission tracking, lead CRM | Real estate agents |
| **Delivery** | $0.50/delivery | Per-delivery platform fee | Logistics/SMEs |

**Target**: 100 paying @ avg $99 = ~$10K MRR in 12 months.

---

## 12. File Change Count

| Category | New Files | Modified Files |
|----------|-----------|----------------|
| Backend: delivery-service | ~35 | 0 |
| Backend: realty-service | ~45 | 0 |
| Backend: restaurant POS | ~6 | ~2 |
| Backend: payment gateway config | ~3 | ~1 |
| Infrastructure | 0 | ~6 |
| Frontend: shared components | ~7 | 0 |
| Frontend: admin pages | ~6 | ~1 (routes) |
| Frontend: POS pages | ~6 | 0 |
| Frontend: Realty pages | ~10 | 0 |
| Frontend: Housing pages | ~6 | 0 |
| Frontend: Delivery pages | ~5 | 0 |
| Frontend: Finance pages | ~4 | 0 |
| Frontend: Reporting pages | ~4 | 0 |
| Frontend: Cross-cutting | ~3 | 0 |
| Frontend: i18n + dark + mobile | ~1 | ~15 |
| Docker/scripts | 0 | ~4 |
| **Total** | **~141** | **~29** |

---

*Generated 2026-06-20. Total: 2 new microservices + 43 new frontend pages + POS + Housing + Cross-cutting UX. Estimated: 7 weeks for complete build.*
