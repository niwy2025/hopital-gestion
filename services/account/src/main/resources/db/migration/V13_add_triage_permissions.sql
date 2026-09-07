INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    ('TRIAGE_READ', 'Consulter les fiches de triage des passages'),
    ('TRIAGE_WRITE', 'Saisir les fiches de triage et les signes vitaux')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('ADMIN', 'TRIAGE_READ'),
    ('ADMIN', 'TRIAGE_WRITE'),
    ('HOSPITAL_ADMIN', 'TRIAGE_READ'),
    ('HOSPITAL_ADMIN', 'TRIAGE_WRITE'),
    ('DOCTOR', 'TRIAGE_READ'),
    ('NURSE', 'TRIAGE_READ'),
    ('NURSE', 'TRIAGE_WRITE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
