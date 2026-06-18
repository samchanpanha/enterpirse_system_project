-- V4__add_customers.sql
-- Adds per-domain customer table for inventory (B2B buyers / customers).
-- Idempotent.

CREATE TABLE IF NOT EXISTS customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    name            VARCHAR(255) NOT NULL,
    contact_person  VARCHAR(255),
    phone           VARCHAR(50),
    email           VARCHAR(255),
    tax_number      VARCHAR(50),
    billing_address TEXT,
    shipping_address TEXT,
    payment_terms   VARCHAR(50),
    credit_limit    NUMERIC(15, 2),
    balance         NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes           TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    total_purchases NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers(active) WHERE active = TRUE;
