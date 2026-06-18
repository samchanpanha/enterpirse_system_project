#!/bin/bash
# Seed development data for the Report System
set -e

DB_USER="${DB_USER:-report_user}"
DB_PASSWORD="${DB_PASSWORD:-report_pass}"
DB_HOST="${DB_HOST:-localhost}"

echo "Seeding development data..."
echo "Creating demo tenant..."

DEMO_TENANT_ID="00000000-0000-0000-0000-000000000001"
DEMO_USER_ID="00000000-0000-0000-0000-000000000002"

PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -U $DB_USER -d auth_db <<SQL
INSERT INTO tenants (id, name, slug, domain, is_active, subscription)
VALUES ('$DEMO_TENANT_ID', 'Demo Company', 'demo', 'demo.example.com', true, 'business')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, is_active, locale)
VALUES (
  '$DEMO_USER_ID',
  '$DEMO_TENANT_ID',
  'admin@demo.com',
  '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'Admin',
  'User',
  true,
  'en'
) ON CONFLICT (tenant_id, email) DO NOTHING;
SQL

echo "Seed data created:"
echo "  Tenant: demo | Login: admin@demo.com / password"
