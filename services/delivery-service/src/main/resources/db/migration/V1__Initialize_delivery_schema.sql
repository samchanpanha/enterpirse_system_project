-- V1__Initialize_delivery_schema.sql

CREATE TABLE deliveries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    outlet_id       UUID,
    order_id        UUID,
    driver_id       UUID,
    customer_name   VARCHAR(255),
    customer_phone  VARCHAR(50),
    delivery_address TEXT,
    pickup_address  TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    scheduled_at    TIMESTAMPTZ,
    picked_up_at    TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    delivery_fee    NUMERIC(10,2) DEFAULT 0,
    distance_km     NUMERIC(8,2),
    notes           TEXT,
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_deliveries_tenant ON deliveries(tenant_id);
CREATE INDEX idx_deliveries_branch ON deliveries(branch_id);
CREATE INDEX idx_deliveries_outlet ON deliveries(outlet_id);
CREATE INDEX idx_deliveries_order ON deliveries(order_id);
CREATE INDEX idx_deliveries_driver ON deliveries(driver_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_tenant_status ON deliveries(tenant_id, status);

CREATE TABLE drivers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    email           VARCHAR(255),
    license_number  VARCHAR(100),
    vehicle_type    VARCHAR(50),
    vehicle_plate   VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'available',
    rating          DECIMAL(3,2) DEFAULT 0,
    total_deliveries INTEGER DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    current_lat     DECIMAL(10,7),
    current_lng     DECIMAL(10,7),
    last_location_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_drivers_tenant ON drivers(tenant_id);
CREATE INDEX idx_drivers_branch ON drivers(branch_id);
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_tenant_active ON drivers(tenant_id, is_active);

CREATE TABLE fleet_vehicles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    name            VARCHAR(255) NOT NULL,
    plate_number    VARCHAR(50),
    vehicle_type    VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'available',
    capacity_kg     DECIMAL(10,2),
    fuel_type       VARCHAR(50),
    insurance_expiry DATE,
    last_maintenance_at DATE,
    next_maintenance_at DATE,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_fleet_vehicles_tenant ON fleet_vehicles(tenant_id);
CREATE INDEX idx_fleet_vehicles_branch ON fleet_vehicles(branch_id);
CREATE INDEX idx_fleet_vehicles_status ON fleet_vehicles(status);

CREATE TABLE delivery_zones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    branch_id       UUID,
    name            VARCHAR(255) NOT NULL,
    name_kh         VARCHAR(255),
    boundaries      JSONB DEFAULT '{}',
    base_fee        NUMERIC(10,2) DEFAULT 0,
    per_km_fee      NUMERIC(8,2) DEFAULT 0,
    min_fee         NUMERIC(10,2) DEFAULT 0,
    max_fee         NUMERIC(10,2),
    estimated_minutes INTEGER,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_delivery_zones_tenant ON delivery_zones(tenant_id);
CREATE INDEX idx_delivery_zones_branch ON delivery_zones(branch_id);
CREATE INDEX idx_delivery_zones_active ON delivery_zones(tenant_id, is_active);

CREATE TABLE delivery_proofs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id     UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    proof_type      VARCHAR(20) NOT NULL DEFAULT 'photo',
    image_url       TEXT,
    signature_data  TEXT,
    notes           TEXT,
    captured_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_delivery_proofs_delivery ON delivery_proofs(delivery_id);
CREATE INDEX idx_delivery_proofs_type ON delivery_proofs(proof_type);

CREATE TABLE driver_payouts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    driver_id       UUID NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    total_deliveries INTEGER DEFAULT 0,
    total_distance  NUMERIC(10,2) DEFAULT 0,
    total_earnings  NUMERIC(12,2) DEFAULT 0,
    commission_amount NUMERIC(12,2) DEFAULT 0,
    bonus_amount    NUMERIC(12,2) DEFAULT 0,
    deduction_amount NUMERIC(12,2) DEFAULT 0,
    net_amount      NUMERIC(12,2) DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_driver_payouts_tenant ON driver_payouts(tenant_id);
CREATE INDEX idx_driver_payouts_driver ON driver_payouts(driver_id);
CREATE INDEX idx_driver_payouts_status ON driver_payouts(status);
CREATE INDEX idx_driver_payouts_period ON driver_payouts(period_start, period_end);
