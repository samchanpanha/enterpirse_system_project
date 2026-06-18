-- V3__add_customers.sql
-- Adds per-domain customer table for property management.
-- Each property tenant has renters (B2C end customers) tracked here.
-- Idempotent.

CREATE TABLE IF NOT EXISTS customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    name            VARCHAR(255) NOT NULL,
    name_kh         VARCHAR(255),
    phone           VARCHAR(50) NOT NULL,
    email           VARCHAR(255),
    national_id     VARCHAR(50),
    address         TEXT,
    occupation      VARCHAR(255),
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    notes           TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    lease_count     INT NOT NULL DEFAULT 0,
    total_paid      NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers(active) WHERE active = TRUE;
