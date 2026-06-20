-- V5__add_ui_permissions.sql
-- Add permission codes that the frontend checks for, and ensure the Admin role owns them.

DO $$
DECLARE
    admin_role_id UUID;
    perm_id UUID;
    demo_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    hq_branch_id UUID := '00000000-0000-0000-0000-000000000010';
BEGIN
    -- Find admin role for demo tenant
    SELECT id INTO admin_role_id FROM roles WHERE tenant_id = demo_tenant_id AND name = 'Admin' LIMIT 1;

    -- Insert new permissions if they don't exist, and map to Admin role
    FOR perm_id IN
        INSERT INTO permissions (id, code, name, module, branch_id)
        VALUES
            (gen_random_uuid(), 'branches.read', 'Read Branches', 'admin', hq_branch_id),
            (gen_random_uuid(), 'branches.write', 'Write Branches', 'admin', hq_branch_id),
            (gen_random_uuid(), 'users.read', 'Read Users', 'admin', hq_branch_id),
            (gen_random_uuid(), 'users.write', 'Write Users', 'admin', hq_branch_id),
            (gen_random_uuid(), 'roles.read', 'Read Roles', 'admin', hq_branch_id),
            (gen_random_uuid(), 'roles.write', 'Write Roles', 'admin', hq_branch_id),
            (gen_random_uuid(), 'permissions.read', 'Read Permissions', 'admin', hq_branch_id),
            (gen_random_uuid(), 'permissions.write', 'Write Permissions', 'admin', hq_branch_id),
            (gen_random_uuid(), 'clients.read', 'Read Clients', 'admin', hq_branch_id),
            (gen_random_uuid(), 'clients.write', 'Write Clients', 'admin', hq_branch_id),
            (gen_random_uuid(), 'features.read', 'Read Features', 'admin', hq_branch_id),
            (gen_random_uuid(), 'features.write', 'Write Features', 'admin', hq_branch_id)
        ON CONFLICT (code) DO NOTHING
        RETURNING id
    LOOP
        IF admin_role_id IS NOT NULL THEN
            INSERT INTO role_permissions (role_id, permission_id, branch_id) VALUES (admin_role_id, perm_id, hq_branch_id)
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;

    -- Ensure any pre-existing UI permissions are also mapped to Admin role
    IF admin_role_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id, branch_id)
        SELECT admin_role_id, p.id, hq_branch_id FROM permissions p
        WHERE p.code IN ('branches.read','branches.write','users.read','users.write',
                         'roles.read','roles.write','permissions.read','permissions.write',
                         'clients.read','clients.write','features.read','features.write')
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
