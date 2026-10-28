-- Lecture du catalogue pour préparer une ordonnance au triage (sans accès aux stocks).
INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles AS role
CROSS JOIN permissions AS permission
LEFT JOIN role_permissions AS existing
    ON existing.role_id = role.id
   AND existing.permission_id = permission.id
WHERE role.code = 'NURSE'
  AND permission.code = 'PHARMACY_CATALOG_READ'
  AND existing.role_id IS NULL;
