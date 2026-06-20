# PLAN-v2.md — Bring Every Backend Feature into the Frontend

> Version 2 of the completion plan. Scope: every service endpoint, every domain entity, every event, every admin operation gets a real frontend page. Goal: zero unused backend capability.
>
> Save location: `PLAN-v2.md` at repo root.
>
> **How to use tomorrow:** pick a phase, work top-to-bottom inside the phase, mark items done in the checklist, run the acceptance gate at the end of each phase.

---

## 0. Goals & principles

- **100% endpoint coverage**: every `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` in every service has at least one UI affordance.
- **3-actor UX**: Client (tenant), User (staff), Customer (B2C end user) — different pages, different nav, different permissions.
- **Mobile-first**: phone/tablet usable for daily ops (POS, stock, maintenance, payroll review).
- **Khmer-first**: every label translatable; default locale switchable per user.
- **Offline-tolerant**: POS queues offline, syncs on reconnect; readonly views work offline.
- **Real-time**: WebSocket/SSE for KDS, notifications, dashboards.
- **Observable**: every action emits a toast; every error is actionable.
- **Accessible**: keyboard nav, ARIA, color contrast, focus traps in modals.

---

## 1. Frontend coverage matrix (current vs required)

Legend: ✅ exists · 🟡 partial · ❌ missing

| Service / Domain | Backend endpoints | Frontend page | Status |
|---|---|---|---|
| **Auth** | | | |
| Login | `POST /auth/login` | `/login` | ✅ |
| Register | `POST /auth/register` | `/register` | ✅ |
| Refresh | `POST /auth/refresh` | (composable) | 🟡 |
| Logout | `POST /auth/logout` | header | 🟡 |
| SSO bridge | `POST /auth/sso-login` | (Keycloak plugin) | ✅ |
| **Users** | `GET/POST/PUT/DELETE /users`, `/users/by-tenant/{id}`, `/users/{id}` | `/app/admin/users` | ❌ |
| **Tenants** | `GET/POST/PUT/DELETE /tenants` | `/app/admin/clients` | 🟡 list only |
| **Branches** | full CRUD + `/by-tenant/{id}` | `/app/admin/branches` | ✅ |
| **User-Branches** | full CRUD + assign | (in branches page) | 🟡 |
| **Features** | tree, enabled, check, enable, disable | `/app/admin/features` | 🟡 static list |
| **Admin Realm** | create realm, create user, health | (none) | ❌ |
| **Property** | | | |
| Properties | full CRUD + by-tenant + by-property | `/app/property`, `/app/property/[id]` | 🟡 edit missing |
| Units | full CRUD + by-property | (in detail page only) | 🟡 |
| Leases | full CRUD + by-unit, by-tenant | ❌ no page | ❌ |
| Schedules | CRUD + by-property | `/app/property/[id]/schedule` | 🟡 create missing |
| Maintenance | CRUD + by-property | `/app/property/[id]/maintenance` | 🟡 create missing |
| Customers (renters) | full CRUD (migration exists, no controller) | ❌ | ❌ |
| **Restaurant** | | | |
| Outlets | full CRUD + by-tenant | ❌ no page | ❌ |
| Tables | CRUD + by-outlet | `/app/restaurant` (POS) | 🟡 no CRUD page |
| Categories | full CRUD | `/app/restaurant/menu` | 🟡 edit/delete TODO |
| MenuItems | full CRUD | `/app/restaurant/menu` | 🟡 edit/delete TODO |
| Orders | CRUD + by-outlet, status workflow | `/app/restaurant/orders`, `/app/restaurant` | ✅ |
| OrderItems | (sub) | (in order detail) | ✅ |
| KDS | WebSocket | `/app/restaurant/kds` | ✅ |
| Customers (CRM) | full CRUD | `/app/restaurant/customers` | 🟡 edit TODO |
| Reservations | full CRUD + by-date | `/app/restaurant/reservations` | ✅ |
| **Inventory** | | | |
| Products | full CRUD | `/app/inventory` | 🟡 edit TODO |
| ProductCategories | CRUD | (in products page) | 🟡 |
| Suppliers | full CRUD | `/app/inventory/suppliers` | 🟡 update/delete TODO |
| Warehouses | CRUD | ❌ | ❌ |
| Stock | entry, exit, current | (modal in products) | 🟡 |
| PurchaseOrders | full CRUD + receive | `/app/inventory/purchase-orders` | 🟡 cancel TODO |
| StockTransfers | full workflow | `/app/inventory/transfers` | ✅ |
| Customers (B2B) | full CRUD (migration exists) | ❌ | ❌ |
| Low-stock alerts | (event-driven) | ❌ | ❌ |
| **Finance** | | | |
| Accounts (CoA) | full CRUD | `/app/finance/accounts` | 🟡 create TODO |
| JournalEntries | CRUD + by-tenant | `/app/finance` (recent) | 🟡 no browser page |
| Invoices | full CRUD + pay | `/app/finance/invoices` | 🟡 create TODO |
| TaxRecords | CRUD + filing | `/app/finance/tax` | ✅ |
| TaxFilingReports | generate | `/app/finance/tax` | ✅ |
| Employees | full CRUD | `/app/finance/employees` | ✅ |
| PayrollPeriods | CRUD + run | `/app/finance/payroll` | ✅ |
| PayrollItems | (sub) | (in payroll) | ✅ |
| Customers (AR/AP) | full CRUD (migration exists) | ❌ | ❌ |
| **Payment** | | | |
| Payments | full CRUD + process | `/app/payment` | ✅ |
| Refunds | create | (action in history) | 🟡 |
| Reconciliations | full workflow | `/app/payment/reconciliation` | ✅ |
| PaymentGatewayConfigs | CRUD | ❌ | ❌ |
| Webhooks (in) | ABA/Wing/Pi Pay | (server) | n/a |
| **Reporting** | | | |
| ReportDefinitions | full CRUD + run | `/app/reports` | ✅ |
| ScheduledReports | CRUD | ❌ no page | ❌ |
| ReportExecutions | get + list | ❌ no history page | ❌ |
| DashboardConfigs | full CRUD | `/app/reports/dashboards` | ✅ |
| AggregatedSnapshots | get | ❌ no viewer | ❌ |
| **Cross-cutting** | | | |
| Onboarding wizard | n/a | ❌ | ❌ |
| Notifications center | n/a | ❌ | ❌ |
| Help / docs | n/a | ❌ | ❌ |
| Profile / settings | n/a | ❌ | ❌ |
| Activity log | n/a | ❌ | ❌ |
| Khmer i18n | n/a | ❌ | ❌ |
| Offline POS | n/a | ❌ | ❌ |

**Totals:** ~95 backend endpoints, 27 frontend pages. ~40 pages missing or partial.

---

## 2. Cross-cutting UI features (build first, use everywhere)

### 2.1 Feedback primitives
- `useNotify()` composable → success/error/info toasts (replace every `alert()`).
- `<ConfirmDialog v-model="open" title="…" message="…" confirm-text="Delete" destructive />` → use everywhere instead of native `confirm()`.
- `<EmptyState title="…" description="…" action-label="Add new" @action="…" />` (refactor of `AdminEmpty` with actions).
- `<SkeletonTable :rows="5" :cols="6" />` → swap every list-page spinner.

### 2.2 Form system (already started in Phase 4)
Extend `useFormSchema` + `AdminForm` to support:
- `select` (with remote search for `customerId`, `productId`, `accountId`)
- `date`, `datetime`, `daterange`
- `money` (currency-aware, locale-aware)
- `textarea`
- `multiselect` (for `branchIds[]`)
- `file` (avatar, attachment → MinIO presigned upload)
- `repeater` (invoice line items, PO items)
- `autocomplete` (Khmer search)

### 2.3 Drawer + modal
- `useDrawer()` ✅ exists → add `useModal()` for centered confirm/info dialogs.
- Trap focus, ESC to close, ARIA labels.

### 2.4 Data table
Extend `AdminTable` with: sortable columns, column filters, row selection (bulk actions), inline edit, expand row (for line items), server-side pagination.

### 2.5 Layouts
- `default.vue` — marketing/landing (no sidebar).
- `auth.vue` — centered card.
- `app.vue` — admin shell (sidebar + header) ✅.

### 2.6 State & data
- All stores use `branchStore.$apiWithBranch()`.
- New stores: `useNotifyStore`, `useUsersStore`, `useTenantsStore`, `useCustomersStore` (per-domain), `useLeasesStore`, `useOutletsStore`, `useWarehousesStore`, `useNotificationsStore`, `useHelpStore`.
- Add `usePagination()`, `useDebouncedSearch()`, `useSortable()` composables.

### 2.7 Realtime
- `useEventStream('/ws/...')` composable (WebSocket auto-reconnect).
- `useSSE('/api/.../stream')` for one-way updates (low-stock, transfer status).
- Wire to: KDS (already), notifications, low-stock banner, transfer status, payment webhook status.

### 2.8 i18n
- `@nuxtjs/i18n` with `en` + `km`.
- All strings in `app/i18n/en.json`, `app/i18n/km.json`.
- `useT(key)` composable.
- Locale switcher in header (persist in `user.preferences.locale`).
- Khmer number/date/currency formatters.

### 2.9 Offline
- `@vite-pwa/nuxt` for service worker + app shell.
- IndexedDB queue (`idb`) for offline mutations.
- `useOnline()` composable.
- Offline banner ("You're offline. Changes will sync.").
- Conflict resolution: per-entity last-write-wins + server timestamp; warn on conflict.

### 2.10 Error handling
- Global error boundary in `app.vue` → toast + reload option.
- `useApiError(err)` → extract `data.message`, fallback to generic.
- `error.vue` page (404, 500, 403, maintenance).

### 2.11 Permissions & features
- `v-can="'invoice.create'"` and `v-can-show` already exist.
- `v-feature="'restaurant.pos'"` already exists.
- Wire in every page action button (hide if no permission, disable if feature off, upgrade modal on click).

---

## 3. Page-by-page build list (ordered by domain)

### Phase A — Auth & users (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| A1 | Login | `/login` | ✅ — add locale switcher, "Forgot password" link, SSO button polish |
| A2 | Register | `/register` | ✅ — add tenant slug validation, terms checkbox |
| A3 | Forgot password | `/forgot-password` | ❌ — POST `/auth/forgot`, sends email |
| A4 | Reset password | `/reset-password?token=…` | ❌ — POST `/auth/reset` |
| A5 | Email verify | `/verify-email?token=…` | ❌ |
| A6 | Profile | `/app/profile` | ❌ — name, email, phone, avatar, locale, password change |
| A7 | Preferences | `/app/preferences` | ❌ — theme (light/dark), locale, timezone, notifications opt-in |
| A8 | User management | `/app/admin/users` | ❌ — list, invite, edit, deactivate, reset password, assign branches |
| A9 | User detail | `/app/admin/users/[id]` | ❌ — same as A8 + activity + branches |
| A10 | Tenant detail | `/app/admin/clients/[id]` | ❌ — tier, features, usage, billing link |
| A11 | Tenant create wizard | `/app/admin/clients/new` | ❌ — multi-step, uses `AdminRealmController` |
| A12 | Admin realm panel | `/app/admin/realms` | ❌ — list Keycloak realms, health, force re-sync |

### Phase B — Property (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| B1 | Property list | `/app/property` | 🟡 — wire edit/delete, add filter by type/city |
| B2 | Property detail | `/app/property/[id]` | ✅ — add unit tab, lease tab, financials |
| B3 | Property create/edit drawer | (in B1) | 🟡 |
| B4 | Units list | `/app/property/[id]/units` | ❌ — list, create, edit, delete; bulk assign |
| B5 | Lease list | `/app/property/leases` | ❌ — all leases across properties |
| B6 | Lease create | `/app/property/leases/new` | ❌ — tenant, unit, dates, rent, deposit |
| B7 | Lease detail | `/app/property/leases/[id]` | ❌ — schedule, payments, terminate/renew |
| B8 | Schedules | `/app/property/[id]/schedule` | 🟡 — add create event drawer |
| B9 | Maintenance | `/app/property/[id]/maintenance` | 🟡 — add create ticket drawer, assign, status workflow |
| B10 | Customers (renters) | `/app/property/customers` | ❌ — directory (Phase C backend required) |
| B11 | Customer detail | `/app/property/customers/[id]` | ❌ — leases, payments, documents |

### Phase C — Per-domain Customers (backend + UI, 3 days) **[BLOCKER for B10/D10/E10]**
Backend (per service: property, inventory, finance):
1. Domain model `Customer`, port, service impl, JPA entity, mapper, repo, adapter, controller.
2. Endpoints: `GET/POST/PUT/DELETE /customers`, `/by-tenant/{id}`, `/{id}`, `/search?q=`.
3. Kafka event `customer.created/updated/deleted` for cross-service sync.
4. Migration: add `customer_id UUID` FK on `invoices`, `orders`, `purchase_orders`, `leases` (nullable for backward compat).
5. Seed 10 sample customers per domain in `seed-sample-data.sh`.

Frontend (3 pages × 3 services + shared):
- `useCustomers(domain)` Pinia store factory.
- List/create/edit/delete using `AdminTable` + `AdminDrawer` + `AdminForm`.
- Customer picker autocomplete (used in invoices, orders, POs, leases).

### Phase D — Restaurant (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| D1 | POS | `/app/restaurant` | ✅ — add barcode scan, split bill, discount, tip |
| D2 | Outlet management | `/app/restaurant/outlets` | ❌ — list, create, edit, delete; tax rate, currency |
| D3 | Table layout editor | `/app/restaurant/tables` | ❌ — drag-drop floor plan, table CRUD |
| D4 | Menu | `/app/restaurant/menu` | 🟡 — wire edit/delete (categories + items), Khmer name required, image upload |
| D5 | Orders | `/app/restaurant/orders` | ✅ — add refund, reprint, void |
| D6 | KDS | `/app/restaurant/kds` | ✅ — add sound, bump, recall, prep-time stats |
| D7 | Customers (CRM) | `/app/restaurant/customers` | 🟡 — wire edit, add loyalty points, visit history, birthday |
| D8 | Reservations | `/app/restaurant/reservations` | ✅ — add reminder, no-show, table assign |
| D9 | QR ordering | `/q/[tableCode]` | ❌ — public, mobile-friendly, no login |
| D10 | Customers directory (B2B) | shared with Phase C | n/a |

### Phase E — Inventory (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| E1 | Products | `/app/inventory` | 🟡 — wire edit/delete, add bulk import (CSV), barcode gen |
| E2 | Product categories | `/app/inventory/categories` | ❌ — tree view, drag-drop reorder |
| E3 | Suppliers | `/app/inventory/suppliers` | 🟡 — wire update/delete, add products-per-supplier view |
| E4 | Warehouses | `/app/inventory/warehouses` | ❌ — list, create, edit, stock per warehouse |
| E5 | Stock in/out | `/app/inventory/stock` | ❌ — dedicated page with history, batch, expiry |
| E6 | Low-stock alerts | `/app/inventory/alerts` | ❌ — items below threshold, quick reorder |
| E7 | Purchase orders | `/app/inventory/purchase-orders` | 🟡 — wire cancel, add receive partial, print |
| E8 | Stock transfers | `/app/inventory/transfers` | ✅ — add approval workflow (manager in source) |
| E9 | Stock take | `/app/inventory/stocktake` | ❌ — count vs system, variance report |
| E10 | Customers (B2B) | shared with Phase C | n/a |

### Phase F — Finance (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| F1 | Dashboard | `/app/finance` | ✅ — add charts (revenue/expense, AR aging, top customers) |
| F2 | Chart of accounts | `/app/finance/accounts` | 🟡 — wire create drawer, add tree view, import CoA template |
| F3 | Journal entries | `/app/finance/journal` | ❌ — browser, filter by account/date, drilldown |
| F4 | Invoices | `/app/finance/invoices` | 🟡 — wire create drawer (line items), send by email, PDF download |
| F5 | Invoice detail | `/app/finance/invoices/[id]` | ❌ — print, send, payments history, credit note |
| F6 | Bills (AP) | `/app/finance/bills` | ❌ — vendor bills, link to PO, payment |
| F7 | Tax records | `/app/finance/tax` | ✅ — add GDT XML export, e-filing button |
| F8 | Tax filing report | `/app/finance/tax/filings/[id]` | ❌ — preview, download XML/CSV, submit status |
| F9 | Employees | `/app/finance/employees` | ✅ — add contract, documents, NSSF number |
| F10 | Payroll | `/app/finance/payroll` | ✅ — add payslip PDF, email, bank file export |
| F11 | Bank reconciliation | `/app/finance/bank-recon` | ❌ — match bank statement lines to journal |
| F12 | Reports (P&L, BS) | `/app/finance/reports` | ❌ — period filter, export PDF/CSV |
| F13 | Customers (AR/AP) | shared with Phase C | n/a |

### Phase G — Payment (1 day)
| # | Page | Route | Notes |
|---|---|---|---|
| G1 | Transactions | `/app/payment` | ✅ — add refund action, send receipt, export |
| G2 | Refunds | `/app/payment/refunds` | ❌ — full list with status, link to original txn |
| G3 | Reconciliation | `/app/payment/reconciliation` | ✅ — add auto-match rules, CSV import |
| G4 | Gateway config | `/app/admin/payment-gateways` | ❌ — ABA/Wing/Pi Pay credentials, test connection, sandbox toggle |
| G5 | Webhook log | `/app/admin/payment-gateways/logs` | ❌ — incoming webhooks, signature failures |

### Phase H — Reporting (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| H1 | Report definitions | `/app/reports` | ✅ — add templates (P&L, BS, sales-by-item, stock-valuation, tax-summary) |
| H2 | Report runner | `/app/reports/[id]/run` | ❌ — parameter form, branch selector, run + download |
| H3 | Scheduled reports | `/app/reports/scheduled` | ❌ — cron editor, email recipients, last-run status |
| H4 | Report execution history | `/app/reports/history` | ❌ — past runs, status, download, re-run |
| H5 | Dashboards | `/app/reports/dashboards` | ✅ — drag-drop widget editor, share link |
| H6 | Dashboard view | `/app/reports/dashboards/[id]` | ❌ — full-screen, auto-refresh, export PDF |
| H7 | Snapshot explorer | `/app/reports/snapshots` | ❌ — historical aggregates, compare two dates |
| H8 | Cross-branch report | `/app/reports/cross-branch` | ❌ — multi-branch aggregation, side-by-side |
| H9 | Bulk export | `/app/reports/bulk-export` | ❌ — export all entities to CSV/ZIP |

### Phase I — Cross-branch admin (1 day)
| # | Page | Route | Notes |
|---|---|---|---|
| I1 | Branches | `/app/admin/branches` | ✅ — add hierarchy (parent), bulk CSV import |
| I2 | User-branch assignment | `/app/admin/user-branches` | ❌ — matrix view, role per branch |
| I3 | Bulk close month | `/app/admin/close-month` | ❌ — close payroll for all branches at once |
| I4 | Bulk ops | `/app/admin/bulk` | ❌ — generic bulk-action runner |

### Phase J — Cross-cutting UX (3 days)
| # | Feature | Notes |
|---|---|---|
| J1 | Notifications center | `/app/notifications` — bell icon in header, mark read, preferences |
| J2 | In-app help | `/help` — markdown-rendered bilingual docs, search |
| J3 | Tooltips & tours | shepherd.js — first-visit tour per page, contextual help |
| J4 | Onboarding wizard | `/onboarding` — for new tenants (steps: company → branch → invite → import) |
| J5 | Activity log | `/app/admin/activity` — every CRUD via Kafka audit topic, filter by user/entity |
| J6 | Khmer i18n | translate all strings, locale switcher, Khmer number/date/currency |
| J7 | Mobile responsive | every page passes 375px and 768px viewports; touch-friendly |
| J8 | Offline POS | service worker + IndexedDB queue + sync indicator |
| J9 | Dark mode | toggle in preferences, persist |
| J10 | Charts | Chart.js wrappers: revenue/expense, top items, AR aging, stock trend |
| J11 | Error pages | 403/404/500/maintenance |
| J12 | Loading skeletons | replace spinners on all 27+ pages |
| J13 | Toasts + confirms | replace all native alert/confirm |

### Phase K — Admin & adoption (2 days)
| # | Page | Route | Notes |
|---|---|---|---|
| K1 | Landing | `/` | marketing hero, value prop, demo video, pricing CTA |
| K2 | Pricing | `/pricing` | 3 tiers, comparison, FAQ, "Start free trial" |
| K3 | About | `/about` | team, mission |
| K4 | Contact | `/contact` | form, email, WhatsApp |
| K5 | Changelog | `/changelog` | auto-generated from git tags |
| K6 | Docs (public) | `/docs` | rendered from `/help` |
| K7 | Demo seed | `docker/scripts/seed-demo.sh` | 1 fresh tenant + 30 days of data |
| K8 | Email templates | SMTP + Mailgun | invite, receipt, payslip, password reset, rent reminder |
| K9 | SMS/WhatsApp | Twilio / 360dialog | rent reminder, payment received, OTP |
| K10 | Billing | `/app/admin/billing` | Stripe + Wing + Pi Pay, tier upgrade, invoices |

---

## 4. Phased execution plan

| Phase | Scope | Days | Depends on | Parallel with |
|---|---|---|---|---|
| **1** | J13, J12, J11, J9, J10 | 2 | — | 7 |
| **2** | Phase A (auth + users) | 2 | 1 | 7 |
| **3** | Phase C (per-domain customers) — **backend first, then UI** | 3 | — | 2, 4, 7 |
| **4** | Phase B (property) | 2 | 1, 3 | 2, 5, 7 |
| **5** | Phase D (restaurant) | 2 | 1, 3 | 4, 6, 7 |
| **6** | Phase E (inventory) | 2 | 1, 3 | 4, 5, 7 |
| **7** | Unit tests across all services | ongoing | — | everything |
| **8** | Phase F (finance) | 2 | 1, 3, 4 | 5, 6, 9 |
| **9** | Phase G (payment) | 1 | 1, 8 | 8, 10 |
| **10** | Phase H (reporting) | 2 | 1, 8 | 8, 9, 11 |
| **11** | Phase I (cross-branch admin) | 1 | 4, 5, 6 | 10, 12 |
| **12** | Phase J (cross-cutting UX) | 3 | 1 | 10, 11 |
| **13** | Phase K (adoption + marketing) | 2 | 12 | — |
| **14** | Observability (Prometheus/Grafana) | 2 | — | 10, 11, 12 |
| **15** | Real payment gateway integrations | 5 | 9, 14 | 13 |
| **16** | PDF/CSV/XML exports | 2 | 8, 10 | 13 |
| **17** | OCR receipt scanning | 3 | 3, 16 | 13 |
| **18** | Khmer i18n full + dark mode | 2 | 1, 12 | 13 |
| **19** | Mobile responsive + offline POS | 3 | 1, 5, 18 | — |
| **20** | Final E2E + accessibility audit + load test | 2 | 1-19 | — |
| **Total** | | **~50-60 days** | | |

---

## 5. Backend gaps that BLOCK frontend (must build first)

These endpoints don't exist yet and pages depend on them:

1. **Per-domain Customer CRUD** (property, inventory, finance) — Phase C, 3 days.
2. **Unit CRUD endpoints** exist in property-service; need to verify and expose via frontend.
3. **Lease CRUD endpoints** exist; no frontend page.
4. **Outlet CRUD** exists in restaurant-service; no frontend page.
5. **Table CRUD** exists; floor plan editor needed.
6. **ProductCategory CRUD** exists in inventory; need tree-view page.
7. **Warehouse CRUD** — verify exists; if not, add migration + entity + service + controller.
8. **Refund CRUD** — verify exists in payment-service; if not, add.
9. **PaymentGatewayConfig CRUD** — verify exists; admin page needed.
10. **ScheduledReport CRUD** — verify exists in reporting; page needed.
11. **ReportExecution history endpoint** — verify exists; history page needed.
12. **AggregatedSnapshot query** — verify exists; explorer page needed.
13. **TaxFilingReport export to GDT XML** — verify exists; download button.
14. **Bank reconciliation endpoints** — likely missing; add or use journal.
15. **Forgot/reset password endpoints** — likely missing; add.
16. **Email verify endpoint** — likely missing; add.
17. **Bulk operations endpoint** — add (close month, bulk import).
18. **Activity/audit log endpoint** — add (Kafka audit topic consumer).
19. **Notifications endpoint** — add (WebSocket + REST).
20. **OCR upload endpoint** — Phase 17, 3 days.

---

## 6. Reusable patterns (lock these in early)

### 6.1 Page template
Every list page follows the same shape:
```vue
<template>
  <AdminPageHeader :title="…" :breadcrumbs="…">
    <template #actions>
      <button @click="openCreate">+ New</button>
    </template>
  </AdminPageHeader>
  <AdminSearchBar v-model="list.search.value" />
  <AdminTable :rows="list.items.value" :columns="cols" :loading="list.loading.value">
    <template #actions="{ row }">
      <button @click="openEdit(row)">Edit</button>
      <button @click="confirmDelete(row)">Delete</button>
    </template>
  </AdminTable>
  <AdminPagination v-model="list.page.value" :total="list.total.value" />
  <AdminDrawer v-model="drawer.open.value" :title="drawer.isEdit() ? 'Edit' : 'Create'">
    <AdminForm :schema="schema" v-model="formData" />
  </AdminDrawer>
  <ConfirmDialog v-model="confirm.open" … />
</template>
```

### 6.2 Store pattern
```ts
export const useXStore = defineStore('x', () => {
  const items = ref<X[]>([])
  const loading = ref(false)
  const branchStore = useBranchStore()
  const notify = useNotify()

  async function fetchX (tenantId: string) { … }
  async function createX (data: Partial<X>) { … }
  async function updateX (id: string, data: Partial<X>) { … }
  async function deleteX (id: string) { … }

  return { items, loading, fetchX, createX, updateX, deleteX }
})
```

### 6.3 Form schema example
```ts
const schema = [
  text('name', 'Name', { required: true }),
  select('customerId', 'Customer', { source: '/customers', searchable: true, required: true }),
  money('amount', 'Amount', { currency: 'USD', required: true }),
  date('issueDate', 'Issue date', { required: true, default: today }),
  repeater('items', 'Line items', [
    select('productId', 'Product', { source: '/products', searchable: true }),
    number('quantity', 'Qty', { min: 1 }),
    money('unitPrice', 'Unit price'),
    money('tax', 'Tax')
  ])
]
```

---

## 7. Acceptance criteria per phase

Every phase ends with:
```bash
# Backend
mvn clean test                          # 0 failures
mvn clean package -DskipTests           # BUILD SUCCESS

# Frontend
cd frontend/report-system-web
npm run typecheck                       # 0 errors
npm run lint                            # 0 errors
npm run build                           # success

# E2E
bash docker/scripts/e2e-smoke-test.sh   # 22/22 + new checks
```

Plus manual: walk every new page, create/edit/delete one record each, verify toast, confirm, error path, mobile viewport.

---

## 8. Risk & dependency map

| Risk | Impact | Mitigation |
|---|---|---|
| Per-domain customer backend missing | blocks 3 services | Phase C runs first (3 days) |
| Khmer translations take time | adoption | use Google Translate + native review; ship 80% then iterate |
| Offline POS sync conflicts | data loss | last-write-wins + server timestamp + conflict UI |
| i18n breaks existing components | regressions | ship in own PR, no merge until all pages green |
| Mobile responsive breaks desktop | regressions | responsive-first design, desktop regression suite |
| OCR accuracy on Khmer receipts | user frustration | Tesseract khm + Google Vision fallback + manual confirm step |
| Real payment API downtime | revenue loss | keep CashAdapter as fallback; circuit breaker |
| Bundle size grows with charts + i18n + PWA | slow load | code-split per route, lazy-load chart lib |

---

## 9. Definition of done (whole project)

- [ ] 95+ backend endpoints all have at least one UI affordance.
- [ ] 70+ frontend pages, all mobile-responsive.
- [ ] Khmer + English UI, switchable.
- [ ] Offline POS syncs correctly.
- [ ] All destructive actions use `<ConfirmDialog>`.
- [ ] All async actions show toast on success/error.
- [ ] All lists use `<SkeletonTable>` while loading.
- [ ] All empty lists use `<EmptyState>`.
- [ ] All buttons respect `v-can` and `v-feature`.
- [ ] Per-domain customer CRUD works in 3 services.
- [ ] Khmer receipt PDF downloadable from invoice.
- [ ] OCR uploads create draft AP invoice.
- [ ] Real ABA sandbox transaction end-to-end.
- [ ] Prometheus + Grafana dashboards live.
- [ ] Unit test coverage > 60% across all services.
- [ ] E2E smoke test 30+/30+ checks.
- [ ] Landing page + pricing + onboarding live.
- [ ] Email/SMS notifications work.
- [ ] `mvn verify && npm run build && e2e` green in CI.
- [ ] Lighthouse mobile score > 80.
- [ ] `axe-core` accessibility audit 0 critical.

---

## 10. Day-1 starter checklist (tomorrow)

```text
[ ]  1. git pull (if pushed)
[ ]  2. docker compose up -d
[ ]  3. bash docker/scripts/wait-healthy.sh
[ ]  4. open http://localhost:3000
[ ]  5. cd frontend/report-system-web
[ ]  6. create app/composables/useNotify.ts
[ ]  7. create app/components/admin/ConfirmDialog.vue
[ ]  8. create app/components/admin/SkeletonTable.vue
[ ]  9. wire 7 page TODOs (Phase 1)
[ ] 10. run npm run typecheck && npm run lint && npm run build
[ ] 11. commit
[ ] 12. start Phase 2 (auth + users)
```

---

*Generated 2026-06-18. Update this file as phases complete.*

create more plan then include into PLAN-v2-.md, I analyices add POS system, sale and rental real estate, housing, properies, delivery,into my system current, I want to help think and build complete me.