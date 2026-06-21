-- V2__add_branches_and_branch_id.sql

CREATE TABLE IF NOT EXISTS branches (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    name_kh         VARCHAR(255),
    branch_type     VARCHAR(50) NOT NULL DEFAULT 'STORE',
    parent_id       UUID REFERENCES branches(id),
    address         TEXT,
    city            VARCHAR(100),
    district        VARCHAR(100),
    province        VARCHAR(100),
    phone           VARCHAR(50),
    email           VARCHAR(255),
    timezone        VARCHAR(50) DEFAULT 'Asia/Phnom_Penh',
    locale          VARCHAR(10) DEFAULT 'km',
    currency        VARCHAR(3) DEFAULT 'USD',
    tax_rate        DECIMAL(5,2),
    logo_url        TEXT,
    settings        JSONB DEFAULT '{}',
    is_active       BOOLEAN NOT NULL DEFAULT true,
    is_default      BOOLEAN NOT NULL DEFAULT false,
    opened_at       DATE,
    closed_at       DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    UNIQUE(tenant_id, code)
);
CREATE INDEX IF NOT EXISTS idx_branches_tenant ON branches(tenant_id);
CREATE INDEX IF NOT EXISTS idx_branches_parent ON branches(parent_id);
CREATE INDEX IF NOT EXISTS idx_branches_tenant_active ON branches(tenant_id, is_active);

INSERT INTO branches (id, tenant_id, code, name, branch_type, is_default, is_active, opened_at, created_at)
VALUES ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001',
        'HQ', 'Headquarters', 'HQ', true, true, CURRENT_DATE, now())
ON CONFLICT (tenant_id, code) DO NOTHING;

CREATE TABLE IF NOT EXISTS user_branches (
    user_id     UUID NOT NULL,
    branch_id   UUID NOT NULL,
    role        VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_default  BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_user_branches_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_branches_user ON user_branches(user_id);
CREATE INDEX IF NOT EXISTS idx_user_branches_branch ON user_branches(branch_id);

DO $$
DECLARE
    rec RECORD;
    hq_branch UUID := '00000000-0000-0000-0000-000000000010';
BEGIN
    FOR rec IN
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name != 'flyway_schema_history'
          AND table_name != 'branches'
          AND table_name != 'user_branches'
          AND table_name NOT LIKE 'pg_%'
    LOOP
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS branch_id UUID', rec.table_name);
        EXECUTE format('UPDATE %I SET branch_id = %L WHERE branch_id IS NULL', rec.table_name, hq_branch);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN branch_id SET NOT NULL', rec.table_name);
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS fk_%I_branch', rec.table_name, rec.table_name);
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT fk_%I_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT', rec.table_name, rec.table_name);
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_branch ON %I(branch_id)', rec.table_name, rec.table_name);
    END LOOP;
END $$;
