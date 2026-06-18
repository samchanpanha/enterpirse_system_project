-- V3__add_stock_transfers.sql
-- Stock transfer workflow: inter-branch inventory movements
-- States: DRAFT → PENDING_APPROVAL → APPROVED → SHIPPED → IN_TRANSIT → RECEIVED → CANCELLED
-- (For MVP, we use a simpler 4-state flow: DRAFT → SHIPPED → RECEIVED → CANCELLED)

CREATE TABLE IF NOT EXISTS stock_transfers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    transfer_number VARCHAR(30) NOT NULL,
    from_branch_id  UUID NOT NULL,
    to_branch_id    UUID NOT NULL,
    from_warehouse_id UUID,
    to_warehouse_id   UUID,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    created_by      UUID,
    approved_by     UUID,
    shipped_by      UUID,
    received_by     UUID,
    shipped_at      TIMESTAMPTZ,
    received_at     TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   TEXT,
    total_items     INTEGER NOT NULL DEFAULT 0,
    total_value     NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    UNIQUE(tenant_id, transfer_number),
    CONSTRAINT fk_st_branches FOREIGN KEY (from_branch_id) REFERENCES branches(id) ON DELETE RESTRICT,
    CONSTRAINT fk_st_to_branches FOREIGN KEY (to_branch_id) REFERENCES branches(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_st_tenant ON stock_transfers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_st_from_branch ON stock_transfers(from_branch_id);
CREATE INDEX IF NOT EXISTS idx_st_to_branch ON stock_transfers(to_branch_id);
CREATE INDEX IF NOT EXISTS idx_st_status ON stock_transfers(status);
CREATE INDEX IF NOT EXISTS idx_st_tenant_status ON stock_transfers(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_st_tenant_created ON stock_transfers(tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS stock_transfer_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id     UUID NOT NULL,
    product_id      UUID NOT NULL,
    quantity        NUMERIC(18,4) NOT NULL,
    received_quantity NUMERIC(18,4) NOT NULL DEFAULT 0,
    unit_cost       NUMERIC(18,4) NOT NULL DEFAULT 0,
    discrepancy_notes TEXT,
    line_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_sti_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfers(id) ON DELETE CASCADE,
    CONSTRAINT fk_sti_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_sti_transfer ON stock_transfer_items(transfer_id);
CREATE INDEX IF NOT EXISTS idx_sti_product ON stock_transfer_items(product_id);
