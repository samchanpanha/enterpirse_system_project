-- V4__add_customers.sql
-- Adds per-domain customer table for finance (AR/AP counterparties).
-- Customers here are the people/companies the tenant transacts with.
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
    type            VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',  -- INDIVIDUAL | BUSINESS
    currency        VARCHAR(10) NOT NULL DEFAULT 'USD',
    credit_limit    NUMERIC(15, 2),
    outstanding     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes           TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_type ON customers(type);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers(active) WHERE active = TRUE;
