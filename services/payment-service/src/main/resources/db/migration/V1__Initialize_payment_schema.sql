-- V1__Initialize_payment_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    invoice_id UUID,
    amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    gateway VARCHAR(50) NOT NULL,
    gateway_ref VARCHAR(255),
    method VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    source_type VARCHAR(50),
    source_id UUID,
    metadata JSONB DEFAULT '{}',
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE gateway_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID REFERENCES payment_transactions(id) ON DELETE CASCADE,
    gateway VARCHAR(50) NOT NULL,
    request_body TEXT,
    response_body TEXT,
    http_status INTEGER,
    duration_ms INTEGER,
    success BOOLEAN,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    transaction_id UUID NOT NULL REFERENCES payment_transactions(id) ON DELETE CASCADE,
    amount NUMERIC(14,2) NOT NULL,
    reason TEXT,
    gateway_ref VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    processed_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE reconciliation_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    statement_date DATE NOT NULL,
    total_expected NUMERIC(14,2),
    total_matched NUMERIC(14,2),
    total_unmatched NUMERIC(14,2),
    status VARCHAR(20) DEFAULT 'pending',
    matched_count INTEGER DEFAULT 0,
    unmatched_count INTEGER DEFAULT 0,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_payments_invoice ON payment_transactions(invoice_id);
CREATE INDEX idx_payments_gateway_status ON payment_transactions(gateway, status);
CREATE INDEX idx_payments_paid ON payment_transactions(paid_at);
CREATE INDEX idx_payments_source ON payment_transactions(source_type, source_id);
CREATE INDEX idx_gateway_logs_transaction ON gateway_logs(transaction_id);
