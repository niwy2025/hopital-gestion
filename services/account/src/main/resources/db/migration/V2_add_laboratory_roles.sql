INSERT INTO roles (code, label)
SELECT role_seed.code, role_seed.label
FROM (VALUES
    ('LABORATORY_TECHNICIAN', 'Technicien de laboratoire'),
    ('LABORATORY_BIOLOGIST', 'Biologiste médical')
) AS role_seed (code, label)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = role_seed.code);

INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    ('LABORATORY_READ', 'Consulter les activités du laboratoire'),
    ('LABORATORY_REQUEST_CREATE', 'Créer les demandes d''analyse'),
    ('LABORATORY_SAMPLE_WRITE', 'Enregistrer les échantillons'),
    ('LABORATORY_RESULT_WRITE', 'Saisir les résultats d''analyse'),
    ('LABORATORY_RESULT_VALIDATE', 'Valider les résultats d''analyse')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('ADMIN', 'LABORATORY_READ'),
    ('ADMIN', 'LABORATORY_REQUEST_CREATE'),
    ('ADMIN', 'LABORATORY_SAMPLE_WRITE'),
    ('ADMIN', 'LABORATORY_RESULT_WRITE'),
    ('ADMIN', 'LABORATORY_RESULT_VALIDATE'),
    ('DOCTOR', 'LABORATORY_READ'),
    ('DOCTOR', 'LABORATORY_REQUEST_CREATE'),
    ('NURSE', 'LABORATORY_READ'),
    ('NURSE', 'LABORATORY_SAMPLE_WRITE'),
    ('LABORATORY_TECHNICIAN', 'LABORATORY_READ'),
    ('LABORATORY_TECHNICIAN', 'LABORATORY_SAMPLE_WRITE'),
    ('LABORATORY_TECHNICIAN', 'LABORATORY_RESULT_WRITE'),
    ('LABORATORY_BIOLOGIST', 'LABORATORY_READ'),
    ('LABORATORY_BIOLOGIST', 'LABORATORY_RESULT_VALIDATE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
