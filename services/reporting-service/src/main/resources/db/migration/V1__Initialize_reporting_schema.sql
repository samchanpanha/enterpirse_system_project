-- V1__Initialize_reporting_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE report_definitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    is_system BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE scheduled_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    report_id UUID NOT NULL REFERENCES report_definitions(id) ON DELETE CASCADE,
    schedule_cron VARCHAR(100) NOT NULL,
    recipients JSONB NOT NULL,
    format VARCHAR(20) DEFAULT 'pdf',
    is_active BOOLEAN DEFAULT true,
    last_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE report_executions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES report_definitions(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    parameters JSONB,
    status VARCHAR(20) DEFAULT 'pending',
    output_url TEXT,
    row_count INTEGER,
    duration_ms INTEGER,
    error_message TEXT,
    requested_by UUID,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE dashboard_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    layout JSONB NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE aggregated_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    snapshot_type VARCHAR(50) NOT NULL,
    snapshot_date DATE NOT NULL,
    data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (tenant_id, snapshot_type, snapshot_date)
);

CREATE INDEX idx_report_executions_report ON report_executions(report_id);
CREATE INDEX idx_report_executions_tenant ON report_executions(tenant_id, status);
CREATE INDEX idx_aggregated_type_date ON aggregated_snapshots(tenant_id, snapshot_type, snapshot_date DESC);
