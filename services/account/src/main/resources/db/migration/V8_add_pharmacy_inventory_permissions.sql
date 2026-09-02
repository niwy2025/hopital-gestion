INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    ('PHARMACY_CATALOG_READ', 'Consulter le catalogue des médicaments'),
    ('PHARMACY_CATALOG_WRITE', 'Créer les médicaments du catalogue'),
    ('PHARMACY_STOCK_READ', 'Consulter les stocks et les entrées'),
    ('PHARMACY_STOCK_WRITE', 'Enregistrer les entrées de stock')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('ADMIN', 'PHARMACY_CATALOG_READ'),
    ('ADMIN', 'PHARMACY_CATALOG_WRITE'),
    ('ADMIN', 'PHARMACY_STOCK_READ'),
    ('ADMIN', 'PHARMACY_STOCK_WRITE'),
    ('PHARMACIST', 'PHARMACY_CATALOG_READ'),
    ('PHARMACIST', 'PHARMACY_CATALOG_WRITE'),
    ('PHARMACIST', 'PHARMACY_STOCK_READ'),
    ('PHARMACIST', 'PHARMACY_STOCK_WRITE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
