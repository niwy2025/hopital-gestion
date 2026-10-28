-- Le triage peut initier une ordonnance (ex. calmants) avant la consultation médicale.
-- L’ordonnance apparaît ensuite dans la file pharmacie en attente de délivrance.
INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles AS role
CROSS JOIN permissions AS permission
LEFT JOIN role_permissions AS existing
    ON existing.role_id = role.id
   AND existing.permission_id = permission.id
WHERE role.code = 'NURSE'
  AND permission.code = 'PRESCRIPTION_WRITE'
  AND existing.role_id IS NULL;
