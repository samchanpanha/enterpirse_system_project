#!/bin/bash
# seed-dev-data.sh — Insert demo tenant + admin user
set -e

psql -v ON_ERROR_STOP=1 --username "$DB_USER" --dbname auth_db <<-EOSQL
    INSERT INTO tenants (id, name, slug, is_active, created_at)
    VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Corp', 'demo-corp', true, now())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO users (id, tenant_id, email, password_hash, display_name, is_active, created_at)
    VALUES (
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'admin@demo.com',
        '\$2a\$10\$dummyhashedpassword',
        'Admin User',
        true,
        now()
    ) ON CONFLICT (id) DO NOTHING;

    INSERT INTO roles (id, tenant_id, name, created_at)
    VALUES (
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'admin',
        now()
    ) ON CONFLICT (id) DO NOTHING;
EOSQL
