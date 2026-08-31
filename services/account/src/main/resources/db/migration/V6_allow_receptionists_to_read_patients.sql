INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles AS role
JOIN permissions AS permission ON permission.code = 'PATIENT_READ'
LEFT JOIN role_permissions AS existing_permission
    ON existing_permission.role_id = role.id AND existing_permission.permission_id = permission.id
WHERE role.code = 'RECEPTIONIST'
  AND existing_permission.role_id IS NULL;
