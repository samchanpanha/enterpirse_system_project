CREATE TABLE properties (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    district VARCHAR(100),
    lat DECIMAL(10,7),
    lng DECIMAL(10,7),
    total_units INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'active',
    owner_name VARCHAR(255),
    owner_phone VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE units (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    property_id UUID NOT NULL REFERENCES properties(id),
    label VARCHAR(100) NOT NULL,
    floor INTEGER,
    bedrooms INTEGER DEFAULT 1,
    bathrooms INTEGER DEFAULT 1,
    area_sqm DECIMAL(10,2),
    rent_amount DECIMAL(12,2),
    deposit_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) DEFAULT 'vacant',
    type VARCHAR(50),
    amenities JSONB DEFAULT '[]',
    images JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE leases (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    unit_id UUID NOT NULL REFERENCES units(id),
    tenant_name VARCHAR(255) NOT NULL,
    tenant_phone VARCHAR(50),
    tenant_email VARCHAR(255),
    id_type VARCHAR(50),
    id_number VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    rent_amount DECIMAL(12,2) NOT NULL,
    deposit_amount DECIMAL(12,2),
    payment_day INTEGER DEFAULT 1,
    status VARCHAR(50) DEFAULT 'active',
    documents JSONB DEFAULT '[]',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE schedules (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    unit_id UUID NOT NULL REFERENCES units(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    interval_type VARCHAR(50) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    recurring_rule JSONB,
    status VARCHAR(50) DEFAULT 'scheduled',
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE maintenance_tickets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    unit_id UUID NOT NULL REFERENCES units(id),
    reported_by VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) DEFAULT 'medium',
    category VARCHAR(50),
    status VARCHAR(50) DEFAULT 'open',
    assigned_to VARCHAR(255),
    cost_estimate DECIMAL(12,2),
    actual_cost DECIMAL(12,2),
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_units_property ON units(property_id);
CREATE INDEX idx_leases_unit_status ON leases(unit_id, status);
CREATE INDEX idx_leases_end_date ON leases(end_date);
CREATE INDEX idx_schedules_unit_time ON schedules(unit_id, start_time);
CREATE INDEX idx_schedules_interval ON schedules(interval_type);
CREATE INDEX idx_maintenance_unit ON maintenance_tickets(unit_id);
CREATE INDEX idx_maintenance_status ON maintenance_tickets(status);
