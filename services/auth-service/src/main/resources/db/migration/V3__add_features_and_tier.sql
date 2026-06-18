-- V3__add_features_and_tier.sql
-- Adds: tier column to tenants, features catalog table, client_features (enabled per client) table.
-- All idempotent. Safe to re-run.

-- ─── 1. Add tier column to tenants ─────────────────────────────────────────
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS tier VARCHAR(20) NOT NULL DEFAULT 'BASIC';
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS trial_ends_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_tenants_tier ON tenants(tier);

-- ─── 2. Create features catalog table ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS features (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    module          VARCHAR(50) NOT NULL,
    tier_required   VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    parent_code     VARCHAR(100) REFERENCES features(code),
    deprecated      BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_features_module ON features(module);
CREATE INDEX IF NOT EXISTS idx_features_parent ON features(parent_code);
CREATE INDEX IF NOT EXISTS idx_features_tier ON features(tier_required);

-- ─── 3. Create client_features table (which features are enabled per client) ─
CREATE TABLE IF NOT EXISTS client_features (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    feature_code    VARCHAR(100) NOT NULL REFERENCES features(code) ON DELETE CASCADE,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    enabled_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    enabled_by      UUID REFERENCES users(id),
    expires_at      TIMESTAMPTZ,
    notes           TEXT,
    UNIQUE(tenant_id, feature_code)
);

CREATE INDEX IF NOT EXISTS idx_client_features_tenant ON client_features(tenant_id);
CREATE INDEX IF NOT EXISTS idx_client_features_feature ON client_features(feature_code);
CREATE INDEX IF NOT EXISTS idx_client_features_enabled ON client_features(enabled) WHERE enabled = TRUE;

-- ─── 4. Seed feature catalog (BASIC, PRO, ENTERPRISE tiers) ─────────────────
-- Inventory module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('inventory.products',         'Products & Stock',          'Basic product catalog and stock tracking',           'inventory', 'BASIC',     1),
    ('inventory.suppliers',        'Suppliers',                  'Manage supplier records and contacts',               'inventory', 'BASIC',     2),
    ('inventory.purchase_orders',  'Purchase Orders',            'Create and receive POs from suppliers',              'inventory', 'BASIC',     3),
    ('inventory.stock_transfers',  'Stock Transfers',            'Move stock between branches',                        'inventory', 'PRO',       4),
    ('inventory.multi_warehouse',  'Multi-Warehouse',            'Track stock across multiple warehouses',             'inventory', 'PRO',       5),
    ('inventory.barcode',          'Barcode Scanning',           'Scan barcodes for fast product lookup',              'inventory', 'ENTERPRISE', 6)
ON CONFLICT (code) DO NOTHING;

-- Restaurant module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('restaurant.pos',            'POS',                        'Point of sale with floor plan',                      'restaurant', 'BASIC',     1),
    ('restaurant.kds',            'Kitchen Display',            'Live order feed to kitchen',                         'restaurant', 'BASIC',     2),
    ('restaurant.menu',           'Menu Management',            'Categories and menu items',                          'restaurant', 'BASIC',     3),
    ('restaurant.orders',         'Order Tracking',             'View and manage orders',                             'restaurant', 'BASIC',     4),
    ('restaurant.reservations',   'Reservations',               'Table reservations',                                 'restaurant', 'BASIC',     5),
    ('restaurant.customers',      'Customer Database',          'Track customer info and history',                    'restaurant', 'BASIC',     6),
    ('restaurant.loyalty',        'Loyalty Program',            'Points, tiers, and rewards',                         'restaurant', 'PRO',       7),
    ('restaurant.delivery',       'Delivery Integration',      'Third-party delivery platforms',                     'restaurant', 'ENTERPRISE', 8)
ON CONFLICT (code) DO NOTHING;

-- Property module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('property.properties',       'Properties',                 'Property and building management',                   'property',   'BASIC',     1),
    ('property.units',            'Units',                      'Individual rental units',                            'property',   'BASIC',     2),
    ('property.leases',           'Leases',                     'Tenant lease contracts',                             'property',   'BASIC',     3),
    ('property.rent_collection',  'Rent Collection',            'Track rent payments and invoicing',                  'property',   'BASIC',     4),
    ('property.maintenance',      'Maintenance Tickets',        'Track maintenance issues',                           'property',   'BASIC',     5),
    ('property.schedule',         'Schedules',                  'Inspection and event scheduling',                    'property',   'BASIC',     6),
    ('property.tenant_portal',    'Tenant Portal',              'Self-service portal for tenants',                    'property',   'PRO',       7)
ON CONFLICT (code) DO NOTHING;

-- Finance module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('finance.accounts',          'Chart of Accounts',          'Account hierarchy and balances',                     'finance',    'BASIC',     1),
    ('finance.invoices',          'Invoices',                   'AR/AP invoicing',                                    'finance',    'BASIC',     2),
    ('finance.tax',               'Tax Management',             'Cambodia tax types and filing',                      'finance',    'BASIC',     3),
    ('finance.employees',         'Employees',                  'Employee records with NSSF',                         'finance',    'BASIC',     4),
    ('finance.payroll',           'Payroll',                    'TOS/NSSF payroll computation',                       'finance',    'BASIC',     5),
    ('finance.journal',          'Journal Entries',            'Double-entry bookkeeping',                           'finance',    'PRO',       6),
    ('finance.budgets',           'Budgets',                    'Department budgets and variance',                    'finance',    'PRO',       7),
    ('finance.audit',             'Audit Trail',                'Full audit log of financial changes',                'finance',    'ENTERPRISE', 8)
ON CONFLICT (code) DO NOTHING;

-- Payment module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('payment.cash',              'Cash',                       'Manual cash payments',                               'payment',    'BASIC',     1),
    ('payment.aba',               'ABA PayWay',                 'Cambodia ABA bank gateway',                          'payment',    'BASIC',     2),
    ('payment.wing',              'Wing',                       'Wing mobile payment',                                'payment',    'BASIC',     3),
    ('payment.pi_pay',            'Pi Pay',                     'Pi Pay mobile wallet',                               'payment',    'BASIC',     4),
    ('payment.reconciliation',    'Reconciliation',             'Match payments to bank statements',                  'payment',    'PRO',       5),
    ('payment.refunds',           'Refunds',                    'Issue refunds to customers',                         'payment',    'BASIC',     6)
ON CONFLICT (code) DO NOTHING;

-- Reporting module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('reports.definitions',       'Report Definitions',        'Custom report definitions',                          'reporting',  'BASIC',     1),
    ('reports.scheduled',         'Scheduled Reports',          'Run reports on a schedule',                          'reporting',  'PRO',       2),
    ('reports.dashboards',        'Dashboards',                 'Build custom dashboards',                            'reporting',  'BASIC',     3),
    ('reports.export',            'Export to PDF/Excel',        'Download reports in common formats',                 'reporting',  'PRO',       4),
    ('reports.cross_branch',      'Cross-Branch Reports',       'Aggregate across all branches',                      'reporting',  'ENTERPRISE', 5)
ON CONFLICT (code) DO NOTHING;

-- Platform / Admin module
INSERT INTO features (code, name, description, module, tier_required, sort_order) VALUES
    ('platform.branches',         'Multi-Branch',               'Manage multiple physical locations',                 'platform',   'BASIC',     1),
    ('platform.users',            'User Management',            'Invite and manage staff users',                      'platform',   'BASIC',     2),
    ('platform.roles',            'Role-Based Access',          'Custom roles with permission codes',                 'platform',   'BASIC',     3),
    ('platform.audit_log',        'Audit Log',                  'Full activity log of user actions',                  'platform',   'PRO',       4),
    ('platform.api_access',       'API Access',                 'Public REST API for integrations',                   'platform',   'PRO',       5),
    ('platform.sso',              'SSO (Keycloak)',             'SAML/OIDC single sign-on',                           'platform',   'PRO',       6),
    ('platform.white_label',      'White Label',                'Custom branding, logo, and colors',                  'platform',   'ENTERPRISE', 7)
ON CONFLICT (code) DO NOTHING;

-- ─── 5. Enable BASIC features for the demo tenant ────────────────────────────
INSERT INTO client_features (tenant_id, feature_code, enabled, enabled_at)
SELECT t.id, f.code, TRUE, NOW()
FROM tenants t
CROSS JOIN features f
WHERE t.slug = 'demo-corp'
  AND f.tier_required = 'BASIC'
  AND f.deprecated = FALSE
ON CONFLICT (tenant_id, feature_code) DO NOTHING;
