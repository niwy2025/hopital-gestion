INSERT INTO roles (code, label)
SELECT role_seed.code, role_seed.label
FROM (VALUES
    ('HOSPITAL_ADMIN', 'Administrateur d’hôpital'),
    ('PHARMACY_ADMIN', 'Administrateur de pharmacie')
) AS role_seed (code, label)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = role_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('HOSPITAL_ADMIN', 'PATIENT_READ'),
    ('HOSPITAL_ADMIN', 'PATIENT_REGISTER'),
    ('HOSPITAL_ADMIN', 'PRESCRIPTION_READ'),
    ('HOSPITAL_ADMIN', 'PRESCRIPTION_WRITE'),
    ('HOSPITAL_ADMIN', 'LABORATORY_READ'),
    ('HOSPITAL_ADMIN', 'PHARMACY_CATALOG_READ'),
    ('HOSPITAL_ADMIN', 'PHARMACY_STOCK_READ'),
    ('PHARMACY_ADMIN', 'PATIENT_READ'),
    ('PHARMACY_ADMIN', 'PRESCRIPTION_READ'),
    ('PHARMACY_ADMIN', 'PRESCRIPTION_EXTERNAL_WRITE'),
    ('PHARMACY_ADMIN', 'PHARMACY_DISPENSE'),
    ('PHARMACY_ADMIN', 'PHARMACY_CATALOG_READ'),
    ('PHARMACY_ADMIN', 'PHARMACY_CATALOG_WRITE'),
    ('PHARMACY_ADMIN', 'PHARMACY_STOCK_READ'),
    ('PHARMACY_ADMIN', 'PHARMACY_STOCK_WRITE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;

-- Migration sans perte pour le rôle technique introduit juste avant :
-- les éventuelles affectations existantes deviennent des administrateurs de pharmacie.
INSERT INTO account_roles (account_id, role_id)
SELECT legacy_assignment.account_id, pharmacy_admin.id
FROM account_roles AS legacy_assignment
JOIN roles AS legacy_role ON legacy_role.id = legacy_assignment.role_id AND legacy_role.code = 'PHARMACY_MANAGER'
JOIN roles AS pharmacy_admin ON pharmacy_admin.code = 'PHARMACY_ADMIN'
ON CONFLICT (account_id, role_id) DO NOTHING;

DELETE FROM account_roles
WHERE role_id = (SELECT id FROM roles WHERE code = 'PHARMACY_MANAGER');

DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'PHARMACY_MANAGER');

DELETE FROM roles WHERE code = 'PHARMACY_MANAGER';
