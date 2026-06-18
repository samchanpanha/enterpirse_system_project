-- V1__Initialize_auth_schema.sql

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    domain VARCHAR(255),
    logo_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    subscription VARCHAR(50),
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    locale VARCHAR(10) DEFAULT 'km',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    UNIQUE (tenant_id, email)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, name)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255),
    module VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);
CREATE INDEX idx_roles_tenant ON roles(tenant_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Seed default permissions
INSERT INTO permissions (id, code, name, module) VALUES
    (gen_random_uuid(), 'tenant.read', 'Read Tenant', 'auth'),
    (gen_random_uuid(), 'tenant.write', 'Write Tenant', 'auth'),
    (gen_random_uuid(), 'user.read', 'Read User', 'auth'),
    (gen_random_uuid(), 'user.write', 'Write User', 'auth'),
    (gen_random_uuid(), 'user.delete', 'Delete User', 'auth'),
    (gen_random_uuid(), 'role.read', 'Read Role', 'auth'),
    (gen_random_uuid(), 'role.write', 'Write Role', 'auth'),
    (gen_random_uuid(), 'property.read', 'Read Property', 'property'),
    (gen_random_uuid(), 'property.write', 'Write Property', 'property'),
    (gen_random_uuid(), 'unit.read', 'Read Unit', 'property'),
    (gen_random_uuid(), 'unit.write', 'Write Unit', 'property'),
    (gen_random_uuid(), 'lease.read', 'Read Lease', 'property'),
    (gen_random_uuid(), 'lease.write', 'Write Lease', 'property'),
    (gen_random_uuid(), 'restaurant.pos', 'POS Access', 'restaurant'),
    (gen_random_uuid(), 'restaurant.menu', 'Manage Menu', 'restaurant'),
    (gen_random_uuid(), 'restaurant.orders', 'Manage Orders', 'restaurant'),
    (gen_random_uuid(), 'inventory.read', 'Read Inventory', 'inventory'),
    (gen_random_uuid(), 'inventory.write', 'Write Inventory', 'inventory'),
    (gen_random_uuid(), 'finance.read', 'Read Finance', 'finance'),
    (gen_random_uuid(), 'finance.write', 'Write Finance', 'finance'),
    (gen_random_uuid(), 'payroll.run', 'Run Payroll', 'finance'),
    (gen_random_uuid(), 'report.read', 'Read Reports', 'reporting'),
    (gen_random_uuid(), 'admin.access', 'Admin Access', 'admin');
