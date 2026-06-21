-- V3__add_gateway_configs.sql

CREATE TABLE IF NOT EXISTS payment_gateway_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    config JSONB DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_sandbox BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_gateway_configs_tenant ON payment_gateway_configs(tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_gateway_configs_tenant_gateway ON payment_gateway_configs(tenant_id, gateway);
