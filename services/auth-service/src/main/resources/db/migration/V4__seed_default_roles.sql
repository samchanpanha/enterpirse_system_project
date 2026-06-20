-- V4__seed_default_roles.sql
-- Seed the demo tenant and default system roles.
-- The demo admin user (admin@demo.com) is automatically assigned the Admin role.

DO $$
DECLARE
    admin_role_id UUID := gen_random_uuid();
    manager_role_id UUID := gen_random_uuid();
    cashier_role_id UUID := gen_random_uuid();
    accountant_role_id UUID := gen_random_uuid();
    viewer_role_id UUID := gen_random_uuid();
    admin_user_id UUID := gen_random_uuid();
    demo_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    hq_branch_id UUID := '00000000-0000-0000-0000-000000000010';
    admin_password_hash TEXT := '$2y$10$6yB9ObLczACvGYCtUUs6DO1jWbm8CMox73MIZ1ZYlwyEGhQJ/w7Z6';
BEGIN
    -- Ensure the demo tenant exists (V1 only creates the table, V2's branch seed assumes the tenant)
    -- V2 adds branch_id NOT NULL to all tables, so branch_id is required here
    INSERT INTO tenants (id, name, slug, is_active, subscription, settings, branch_id, created_at)
    VALUES (demo_tenant_id, 'Demo Corp', 'demo-corp', true, 'premium', '{}', hq_branch_id, NOW())
    ON CONFLICT (id) DO NOTHING;

    -- Create the demo admin user (password: Demo123!)
    INSERT INTO users (id, tenant_id, branch_id, email, password_hash, first_name, last_name, is_active, created_at)
    VALUES (admin_user_id, demo_tenant_id, hq_branch_id, 'admin@demo.com', admin_password_hash, 'Admin', 'User', true, NOW())
    ON CONFLICT (tenant_id, email) DO NOTHING;

    -- Create system roles for the demo tenant
    INSERT INTO roles (id, tenant_id, branch_id, name, description, is_system, created_at) VALUES
        (admin_role_id, demo_tenant_id, hq_branch_id, 'Admin', 'Full system access', true, NOW()),
        (manager_role_id, demo_tenant_id, hq_branch_id, 'Manager', 'Manager access to property, restaurant, and inventory', true, NOW()),
        (cashier_role_id, demo_tenant_id, hq_branch_id, 'Cashier', 'POS and order management', true, NOW()),
        (accountant_role_id, demo_tenant_id, hq_branch_id, 'Accountant', 'Finance, payroll, and tax read/write', true, NOW()),
        (viewer_role_id, demo_tenant_id, hq_branch_id, 'Viewer', 'Read-only access across modules', true, NOW());

    -- Admin role gets all permissions
    INSERT INTO role_permissions (role_id, permission_id, branch_id)
    SELECT admin_role_id, id, hq_branch_id FROM permissions;

    -- Manager role
    INSERT INTO role_permissions (role_id, permission_id, branch_id)
    SELECT manager_role_id, id, hq_branch_id FROM permissions
    WHERE code IN ('property.read','property.write','unit.read','unit.write','lease.read','lease.write',
                   'restaurant.pos','restaurant.menu','restaurant.orders',
                   'inventory.read','inventory.write','report.read');

    -- Cashier role
    INSERT INTO role_permissions (role_id, permission_id, branch_id)
    SELECT cashier_role_id, id, hq_branch_id FROM permissions
    WHERE code IN ('restaurant.pos','restaurant.orders','inventory.read');

    -- Accountant role
    INSERT INTO role_permissions (role_id, permission_id, branch_id)
    SELECT accountant_role_id, id, hq_branch_id FROM permissions
    WHERE code IN ('finance.read','finance.write','payroll.run','report.read');

    -- Viewer role
    INSERT INTO role_permissions (role_id, permission_id, branch_id)
    SELECT viewer_role_id, id, hq_branch_id FROM permissions
    WHERE code LIKE '%.read';

    -- Assign Admin role to the demo admin user
    INSERT INTO user_roles (user_id, role_id, branch_id) VALUES (admin_user_id, admin_role_id, hq_branch_id)
    ON CONFLICT (user_id, role_id) DO NOTHING;
END $$;
