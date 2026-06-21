# PLAN-v2-.md — Enterprise SaaS Expansion

> **Bold vision**: Transform Report System into a full-stack enterprise SaaS platform with POS, Real Estate (sales + rentals + property management), Delivery logistics, and Housing management — *all connected to the existing multi-tenant financial core*.
>
> Revenue model: SaaS subscriptions per module + transaction fees (payment + delivery) + premium features.

---

## 0. Revenue Model (How We Make Money)

| Tier | Price | Modules | Target |
|------|-------|---------|--------|
| **Starter** | $29/mo | Auth, Basic Reports, 1 Branch, 3 Users | Small biz |
| **Growth** | $79/mo | +Property, +Restaurant POS, +Inventory, 5 Branches | Mid-market |
| **Enterprise** | $199/mo | All modules + Delivery + Housing + Real Estate Sales, Unlimited branches | Enterprise |
| **POS Lite** | $9/mo/outlet | Standalone POS, offline mode, basic reporting | F&B stalls |
| **Realty** | $49/mo/agent | MLS integration, Commission tracking, Lead CRM | Real estate agents |
| **Delivery** | $0.50/delivery | Per-delivery fee on platform | Logistics companies |

> **Target**: 100 paying tenants @ avg $99/mo = $9,900/mo MRR within 12 months.

---

## 1. New Modules (Beyond Original PLAN-v2.md)

### Module X: Full POS System (F&B + Retail)

**Why**: The restaurant module is admin-only. Real POS drives daily revenue.

| # | Feature | Route | Backend Needed |
|---|---------|-------|----------------|
| X1 | **POS Terminal** — cart-based order entry | `/pos/:outletId` | ✅ endpoints exist |
| X2 | **Barcode scanning** — product lookup + add | (in POS) | 🟡 need scan endpoint |
| X3 | **Split bill** — equal/custom split across payers | (in POS) | ❌ split logic |
| X4 | **Discounts & promos** — %/$ off, BOGO, happy hour | (in POS) | 🟡 promo engine |
| X5 | **Tips** — preset % or custom | (in POS) | ❌ tip handling |
| X6 | **Multiple payment split** — cash + card + KH QR | (in POS) | 🟡 partial |
| X7 | **Kitchen Display System (KDS)** — real-time order tickets | `/pos/kds/:outletId` | ✅ WebSocket exists |
| X8 | **KDS bump bar** — mark items complete, recall | (in KDS) | ❌ bump API |
| X9 | **Offline POS** — IndexedDB queue, sync on reconnect | (PWA) | ❌ sync API |
| X10 | **Receipt printing** — thermal + email + SMS | (in POS) | ❌ print API |
| X11 | **Quick cashier** — no-login quick sale mode | `/pos/quick` | ❌ quick auth |
| X12 | **Daily sales summary** — X-report, Z-report | `/pos/reports/:outletId` | ❌ aggregation |
| X13 | **Table management** — floor plan, merge/split, transfer | `/pos/tables/:outletId` | 🟡 partial |
| X14 | **Customer display** — guest-facing total + promo screen | (in POS) | ❌ display API |
| X15 | **Self-service kiosk mode** — tablet ordering | `/kiosk/:outletId` | ❌ kiosk API |

**Priority blockers**:
- X7 (KDS) — builds on existing restaurant WebSocket
- X9 (Offline) — service worker + IndexedDB queue
- X1 (POS Terminal) — UI shell connects existing order creation

---

### Module Y: Delivery System (Food + Goods + Parcel)

**Why**: Delivery is the highest-growth adjaceny to POS and inventory.

| # | Feature | Route | Backend Needed |
|---|---------|-------|----------------|
| Y1 | **Dispatch board** — new orders, assign driver, tracking | `/delivery/dispatch` | ❌ dispatch API |
| Y2 | **Driver app** — mobile-first, accept/reject, navigate | `/delivery/driver` | ❌ driver endpoints |
| Y3 | **Real-time tracking** — WebSocket position updates | (embedded) | ❌ WebSocket |
| Y4 | **Delivery zones** — geofence pricing, ETA estimation | `/delivery/zones` | ❌ zone CRUD |
| Y5 | **Pricing engine** — distance + weight + surge | `/delivery/pricing` | ❌ pricing API |
| Y6 | **Customer tracking** — public link, status updates | `/track/:orderId` | ❌ public endpoint |
| Y7 | **Driver management** — onboarding, documents, payout | `/delivery/drivers` | ❌ driver CRUD |
| Y8 | **Delivery fleet** — vehicle tracking, maintenance | `/delivery/fleet` | ❌ fleet API |
| Y9 | **Scheduled delivery** — future date/time | (in dispatch) | ❌ scheduling |
| Y10 | **Multi-stop routes** — optimize driver route | `/delivery/routes` | ❌ route engine |
| Y11 | **Proof of delivery** — photo, signature capture | (in driver app) | ❌ upload API |
| Y12 | **Payout reconciliation** — driver earnings, weekly settlement | `/delivery/payouts` | ❌ payout API |
| Y13 | **Customer ratings** — rate driver, dispute resolution | `/delivery/ratings` | ❌ ratings API |
| Y14 | **Integration with POS** — auto-dispatch when order placed | (integration) | ❌ webhook/internal |
| Y15 | **Integration with Inventory** — warehouse to customer | (integration) | ❌ webhook/internal |

**Priority blockers**:
- Y1-Y3 (Dispatch + Driver + Tracking) — core MVP for delivery
- Y5 (Pricing) — distance matrix API integration (Google Maps / OpenRouteService)
- Y14-Y15 — cross-module Kafka events

---

### Module Z: Real Estate Sales + Rentals + Property Management

**Why**: Extends existing property module from management-only to full sales/rental marketplace.

| # | Feature | Route | Backend Needed |
|---|---------|-------|----------------|
| Z1 | **Property listings** — buy/sell/rent marketplace | `/realty/listings` | ❌ listing CRUD |
| Z2 | **Listing detail** — photos, virtual tour, floor plan, map | `/realty/listings/:id` | ❌ listing detail |
| Z3 | **Property search** — filters (price, location, type, size) | `/realty/search` | ❌ search API |
| Z4 | **Saved searches & alerts** — email/SMS on new matches | `/realty/alerts` | ❌ alert CRUD |
| Z5 | **Agent CRM** — leads, tours, offers, commission | `/realty/agents` | ❌ agent CRM |
| Z6 | **Lead management** — inquiry → tour → offer → closed | `/realty/leads` | ❌ lead pipeline |
| Z7 | **Tour scheduling** — agent availability, calendar sync | `/realty/tours` | ❌ tour API |
| Z8 | **Offer management** — submit, counter, accept/reject | `/realty/offers` | ❌ offer workflow |
| Z9 | **Commission calculator** — split, tiers, referral fees | `/realty/commission` | ❌ calc API |
| Z10 | **MLS integration** — import/export listings | `/realty/mls` | ❌ MLS sync |
| Z11 | **Property valuation** — comps, price trend, estimate | `/realty/valuation` | ❌ valuation API |
| Z12 | **Rental applications** — tenant screening, credit check | `/realty/rental/apply` | ❌ application API |
| Z13 | **Lease signing** — digital signature (DocuSign-style) | `/realty/leases/:id/sign` | ❌ e-sign API |
| Z14 | **Rent collection** — auto-charge, late fee, receipts | `/realty/rental/payments` | ❌ payment integration |
| Z15 | **Property marketing** — social auto-post, website embed | `/realty/marketing` | ❌ marketing API |
| Z16 | **HOA management** — fees, violations, board voting | `/realty/hoa` | ❌ HOA CRUD |
| Z17 | **Maintenance marketplace** — vendor bids, work orders | `/realty/maintenance` | 🟡 partial |
| Z18 | **Investor dashboard** — ROI, occupancy, cash flow | `/realty/investor` | ❌ aggregation |
| Z19 | **Rental insurance** — partner API integration | `/realty/insurance` | ❌ partner API |

**Priority blockers**:
- Z1-Z3 (Listings + Search) — MVP for realty marketplace
- Z6 (Leads) — CRM pipeline; reuses existing `Customer` model
- Z8 (Offers) — workflow engine

---

### Module W: Housing & Community Management

**Why**: Adjacent to property — condo/apt building management is a recurring revenue SaaS vertical.

| # | Feature | Route | Backend Needed |
|---|---------|-------|----------------|
| W1 | **Resident directory** — occupants, contact, emergency | `/housing/residents` | ❌ resident CRUD |
| W2 | **Amenity booking** — pool, gym, function room calendar | `/housing/amenities` | ❌ booking API |
| W3 | **Visitor management** — QR code pass, pre-approve | `/housing/visitors` | ❌ visitor API |
| W4 | **Package tracking** — courier logging, pickup notification | `/housing/packages` | ❌ package API |
| W5 | **Community board** — announcements, events, polls | `/housing/community` | ❌ board API |
| W6 | **Maintenance requests** — tenant submit, mgmt assign | `/housing/maintenance` | 🟡 partial |
| W7 | **Service provider portal** — vendor bid on jobs | `/housing/vendors` | ❌ vendor CRUD |
| W8 | **Parking management** — spot assignment, guest passes | `/housing/parking` | ❌ parking API |
| W9 | **Storage unit rental** — locker assignment, billing | `/housing/storage` | ❌ storage API |
| W10 | **Common area reservation** — party room, BBQ pit | `/housing/common-areas` | ❌ reservation API |
| W11 | **HOA dues** — monthly fee, late fee, payment tracking | `/housing/dues` | ❌ dues API |
| W12 | **Violation tracking** — issue, warning, fine | `/housing/violations` | ❌ violation API |
| W13 | **Board elections** — nominee, vote, result | `/housing/elections` | ❌ election API |
| W14 | **Insurance certificates** — unit owner, renew reminder | `/housing/insurance` | ❌ certificate CRUD |
| W15 | **Energy/utility tracking** — submeter, bill allocation | `/housing/utilities` | ❌ utility API |
| W16 | **Security patrol log** — guard checkpoints, incidents | `/housing/security` | ❌ patrol API |
| W17 | **Move-in/move-out** — inspection checklist, deposit | `/housing/move` | ❌ move API |
| W18 | **Document center** — bylaws, rules, meeting minutes | `/housing/documents` | ❌ document API |

**Priority blockers**:
- W1 (Resident directory) — extends existing Customer model
- W2 (Amenity booking) — extends Reservation concept
- W11 (HOA dues) — extends Invoice/payment system

---

## 2. Cross-cutting Enterprise Features (Profit Drivers)

| # | Feature | Why Money |
|---|---------|-----------|
| E1 | **Multi-tenant white-label** — tenant gets own domain + branding | Premium tier upsell |
| E2 | **Invoice branding** — tenant logo, color, custom footer | Paid add-on |
| E3 | **Bulk SMS/Email** — marketing campaigns via Twilio/SendGrid | Per-message revenue |
| E4 | **API marketplace** — public REST API for integrations | Developer tier $49/mo |
| E5 | **Report builder** — drag-drop custom report builder | Enterprise feature |
| E6 | **Automation engine** — IF-ELSE workflows (e.g., "if rent late → send SMS → add fee") | Premium add-on |
| E7 | **Audit trail** — full compliance logging | Compliance tier upsell |
| E8 | **Two-factor auth** — TOTP, SMS OTP | Security add-on $5/mo |
| E9 | **Customer portal** — tenant's customers login, view invoices/payments | Stickiness |
| E10 | **Payment links** — shareable pay-by-link (Wing, ABA, Pi Pay) | Transaction fee |
| E11 | **POS hardware integration** — Star Micronics, Epson, Sunmi | Hardware margin |
| E12 | **Chat support** — in-app Intercom-style messaging | Premium support tier |

---

## 3. Enterprise Login Form Design

### UX Requirements
- **Split screen**: left = hero brand + animated SaaS illustration / right = login card
- **Locale toggle**: EN / KH (Khmer) in top-right corner
- **Dark/Light mode**: moon/sun toggle in top-left
- **"Remember me"** checkbox
- **"Forgot password?"** link
- **SSO button**: "Sign in with Keycloak" styled as secondary CTA
- **Demo credentials hint**: subtle "Demo: admin@demo.com / Demo123!"
- **Trust signals**: "Secured by 256-bit encryption" footer text
- **Responsive**: full-width form on mobile, split on tablet+
- **Animation**: subtle fade-in on card, gradient animation on hero

### Implementation
- Rewrite `LoginComponent` with split-screen layout
- Add locale service stub for future i18n
- Add dark mode support (check `.dark` class)
- Keep all existing AuthService integration

---

## 4. Updated Phased Execution (New Modules Integrated)

| Phase | Scope | Days | Revenue Impact |
|-------|-------|------|----------------|
| **P0** | Enterprise login redesign + locale switcher | 1 | Trust/Conversion |
| **P1** | Full POS Terminal (cart, payment split, KDS) | 5 | **$9-79/mo** |
| **P2** | Offline POS + Receipt printing | 3 | **$9/mo POS Lite** |
| **P3** | Real Estate Listings + Search + Agent CRM | 5 | **$49/mo Realty** |
| **P4** | Housing Management (residents, amenities, dues) | 4 | **$79/mo Growth** |
| **P5** | Delivery Dispatch + Driver App + Tracking | 5 | **$0.50/delivery** |
| **P6** | Real Estate Sales (offers, MLS, valuation) | 4 | **$49/mo Realty** |
| **P7** | Rental marketplace + lease signing | 3 | **$79/mo Growth** |
| **P8** | Delivery fleet + routes + payouts | 3 | **$0.50/delivery** |
| **P9** | Cross-cutting: automation, 2FA, audit | 4 | **$5-49/mo upsell** |
| **P10** | White-label + API marketplace | 4 | **$49/mo Developer** |
| **P11** | Payment links + POS hardware | 3 | **Transaction fee** |
| **P12** | Customer portal + chat support | 3 | **Stickiness** |
| **P13** | Marketing + Landing + Pricing page | 2 | **Conversion** |
| **Total** |  | **~45 days** | **~$10K MRR target** |

---

## 5. Technical Architecture Decisions

### Frontend (Angular 19 + PrimeNG)
- **POS module**: new `features/pos/` with lazy routes, offline-capable via IndexedDB
- **POS cart store**: `@ngrx/signals` or `signal`-based store for cart state
- **Service worker**: `@angular/pwa` for offline POS + push notifications
- **Real-time**: WebSocket via `RxJS` `webSocket` — shared across KDS, dispatch, tracking
- **Printing**: `@thiagoelg/ngx-print` or direct thermal ESC/POS for browser printing
- **Barcode**: `quagga2` or `html5-qrcode` for camera scanning
- **Maps**: Leaflet (free) or MapLibre for delivery tracking + property maps
- **i18n**: `@angular/localize` + custom language service (Khmer/English)
- **Charts**: Chart.js via `ng2-charts` for dashboards

### Backend (Spring Boot — new services needed)
- **Delivery Service**: new module `services/delivery-service` (port 8088)
- **Realty Service**: new module `services/realty-service` (port 8089)
- **POS enhancements**: extend `restaurant-service` with POS endpoints
- **Housing**: extend `property-service` with community endpoints
- **Kafka**: new topics: `delivery.created`, `delivery.position`, `realty.listing.changed`, `housing.maintenance.requested`

### Gateway routes (`RouteConfig.java`)
```
/api/delivery/** → lb://delivery-service/deliveries/**
/api/realty/** → lb://realty-service/realty/**
/api/pos/** → lb://restaurant-service/pos/**
```

---

## 6. Backend Gaps (New Services)

### Delivery Service (port 8088) — NEW
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/deliveries` | GET/POST | List/Create delivery |
| `/deliveries/{id}` | GET/PUT | Detail/Update status |
| `/deliveries/{id}/assign` | PUT | Assign driver |
| `/deliveries/{id}/track` | GET | Get position |
| `/deliveries/{id}/proof` | POST | Upload POD |
| `/drivers` | GET/POST | Driver CRUD |
| `/drivers/{id}/location` | PUT | Update driver GPS |
| `/drivers/{id}/payout` | GET | Driver earnings |
| `/zones` | GET/POST | Zone CRUD |
| `/zones/{id}/pricing` | PUT | Zone pricing |
| `/fleet` | GET/POST | Vehicle CRUD |

### Realty Service (port 8089) — NEW
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/listings` | GET/POST | Listing CRUD |
| `/listings/{id}` | GET/PUT/DELETE | Listing detail/edit/delete |
| `/listings/{id}/images` | POST | Upload images |
| `/listings/search` | GET | Advanced search |
| `/leads` | GET/POST | Lead CRUD |
| `/leads/{id}/status` | PUT | Pipeline stage |
| `/tours` | GET/POST | Tour scheduling |
| `/offers` | GET/POST | Offer management |
| `/offers/{id}/respond` | PUT | Accept/Counter/Reject |
| `/agents` | GET/POST | Agent CRUD |
| `/agents/{id}/commission` | GET | Commission calc |
| `/valuation/{propertyId}` | GET | Price estimate |

---

## 7. Acceptance Criteria

```bash
# Backend
mvn clean test                          # 0 failures
mvn clean package -DskipTests           # BUILD SUCCESS

# Frontend
cd frontend/report-system-angular
npm run build                           # success

# Docker
docker compose --project-directory . -f docker/docker-compose.yml up -d --build
bash docker/scripts/e2e-smoke-test.sh   # 22/22 + new module checks

# Revenue checklist
# ☐ POS can take an order, split bill, take payment
# ☐ Realty listing can be created, searched, lead submitted
# ☐ Delivery can be dispatched, driver assigned, tracked
# ☐ Housing: resident check-in, amenity booked, dues paid
# ☐ Enterprise login shows locale + SSO + dark mode
```

---

## 8. Day-1 Starter (Tomorrow)

```text
[ ] 1. Redesign LoginComponent → enterprise split-screen
[ ] 2. Add locale switcher (EN/KH) to login + topbar
[ ] 3. Create POS module scaffold: /pos route, cart signal store
[ ] 4. Build POS Terminal UI: item grid, cart, payment summary
[ ] 5. Wire POS to existing restaurant order/payment endpoints
[ ] 6. Create delivery service backend scaffold (port 8088)
[ ] 7. Create realty service backend scaffold (port 8089)
[ ] 8. Add gateway routes for new services
[ ] 9. Build + deploy Docker full stack
[ ] 10. Walk through every new page, fix issues
```

---

*Generated 2026-06-20. Start with P0 (Enterprise Login), then P1 (POS).*
