-- La vérification du personnel responsable du passage reste effectuée par le
-- service Patient. Cette permission permet seulement au médecin responsable
-- d'atteindre cette vérification via les interfaces autorisées.
INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles AS role
JOIN permissions AS permission ON permission.code = 'TRIAGE_WRITE'
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role.code = 'DOCTOR'
  AND role_permission.role_id IS NULL;
