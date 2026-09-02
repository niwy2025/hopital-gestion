-- Le pharmacien consulte le catalogue et le stock de son hôpital.
-- La création du catalogue et les entrées de stock restent réservées à l'administration
-- en attendant un rôle dédié de gestionnaire de stock.
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'PHARMACIST')
  AND permission_id IN (
      SELECT id
      FROM permissions
      WHERE code IN ('PHARMACY_CATALOG_WRITE', 'PHARMACY_STOCK_WRITE')
  );
