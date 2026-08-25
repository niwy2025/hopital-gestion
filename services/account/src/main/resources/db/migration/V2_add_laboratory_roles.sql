INSERT INTO roles (code, label)
SELECT role_seed.code, role_seed.label
FROM (VALUES
    (N'LABORATORY_TECHNICIAN', N'Technicien de laboratoire'),
    (N'LABORATORY_BIOLOGIST', N'Biologiste médical')
) AS role_seed (code, label)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = role_seed.code);

INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    (N'LABORATORY_READ', N'Consulter les activités du laboratoire'),
    (N'LABORATORY_REQUEST_CREATE', N'Créer les demandes d''analyse'),
    (N'LABORATORY_SAMPLE_WRITE', N'Enregistrer les échantillons'),
    (N'LABORATORY_RESULT_WRITE', N'Saisir les résultats d''analyse'),
    (N'LABORATORY_RESULT_VALIDATE', N'Valider les résultats d''analyse')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    (N'ADMIN', N'LABORATORY_READ'),
    (N'ADMIN', N'LABORATORY_REQUEST_CREATE'),
    (N'ADMIN', N'LABORATORY_SAMPLE_WRITE'),
    (N'ADMIN', N'LABORATORY_RESULT_WRITE'),
    (N'ADMIN', N'LABORATORY_RESULT_VALIDATE'),
    (N'DOCTOR', N'LABORATORY_READ'),
    (N'DOCTOR', N'LABORATORY_REQUEST_CREATE'),
    (N'NURSE', N'LABORATORY_READ'),
    (N'NURSE', N'LABORATORY_SAMPLE_WRITE'),
    (N'LABORATORY_TECHNICIAN', N'LABORATORY_READ'),
    (N'LABORATORY_TECHNICIAN', N'LABORATORY_SAMPLE_WRITE'),
    (N'LABORATORY_TECHNICIAN', N'LABORATORY_RESULT_WRITE'),
    (N'LABORATORY_BIOLOGIST', N'LABORATORY_READ'),
    (N'LABORATORY_BIOLOGIST', N'LABORATORY_RESULT_VALIDATE')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
