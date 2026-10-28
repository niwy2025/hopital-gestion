-- Le triage est une activité infirmière (ou d'administration hospitalière).
-- Le médecin consulte les données transmises, sans formulaire de saisie.
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'DOCTOR')
  AND permission_id = (SELECT id FROM permissions WHERE code = 'TRIAGE_WRITE');
