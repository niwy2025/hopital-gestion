INSERT INTO roles (code, label)
SELECT role_seed.code, role_seed.label
FROM (VALUES
    ('HR_MANAGER', 'Gestionnaire du personnel')
) AS role_seed (code, label)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = role_seed.code);

INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    ('PERSONNEL_READ', 'Consulter le registre du personnel'),
    ('PERSONNEL_WRITE', 'Enregistrer et modifier le personnel')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('ADMIN', 'PERSONNEL_READ'),
    ('ADMIN', 'PERSONNEL_WRITE'),
    ('HR_MANAGER', 'PERSONNEL_READ'),
    ('HR_MANAGER', 'PERSONNEL_WRITE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
