-- Les rôles de comptabilité sont tous affectés à un établissement par le compte.
-- La séparation entre saisie, encaissement et validation évite qu'une même
-- personne puisse enregistrer puis valider seule une opération sensible.
INSERT INTO roles (code, label)
SELECT role_seed.code, role_seed.label
FROM (VALUES
    ('BILLING_OFFICER', 'Agent de facturation'),
    ('CASHIER', 'Caissier·ère'),
    ('HOSPITAL_ACCOUNTANT', 'Comptable d’hôpital'),
    ('FINANCE_MANAGER', 'Responsable financier·ère'),
    ('FINANCE_AUDITOR', 'Auditeur·rice financier·ère')
) AS role_seed (code, label)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = role_seed.code);

INSERT INTO permissions (code, description)
SELECT permission_seed.code, permission_seed.description
FROM (VALUES
    ('ACCOUNTING_CHART_READ', 'Consulter le plan comptable et les paramètres de facturation'),
    ('ACCOUNTING_JOURNAL_READ', 'Consulter les journaux et écritures comptables'),
    ('ACCOUNTING_JOURNAL_WRITE', 'Préparer et enregistrer les écritures comptables'),
    ('ACCOUNTING_JOURNAL_VALIDATE', 'Valider les écritures comptables'),
    ('ACCOUNTING_INVOICE_READ', 'Consulter les factures et états de paiement'),
    ('ACCOUNTING_INVOICE_WRITE', 'Créer et modifier les factures avant validation'),
    ('ACCOUNTING_INVOICE_VALIDATE', 'Valider ou annuler les factures'),
    ('ACCOUNTING_PAYMENT_READ', 'Consulter les encaissements et paiements'),
    ('ACCOUNTING_PAYMENT_WRITE', 'Enregistrer les encaissements et paiements'),
    ('ACCOUNTING_CASH_READ', 'Consulter les caisses, ouvertures, fermetures et écarts'),
    ('ACCOUNTING_CASH_OPEN', 'Ouvrir une caisse'),
    ('ACCOUNTING_CASH_CLOSE', 'Clôturer une caisse et déclarer les écarts'),
    ('ACCOUNTING_REPORT_READ', 'Consulter les journaux, balances et états financiers'),
    ('ACCOUNTING_ANNEX_READ', 'Consulter les annexes des états financiers'),
    ('ACCOUNTING_ANNEX_WRITE', 'Préparer les annexes des états financiers'),
    ('ACCOUNTING_ANNEX_VALIDATE', 'Valider les annexes des états financiers'),
    ('ACCOUNTING_PERIOD_CLOSE', 'Clôturer un exercice ou une période comptable'),
    ('ACCOUNTING_CONFIGURATION_WRITE', 'Administrer la configuration comptable de l’établissement'),
    ('ACCOUNTING_AUDIT_READ', 'Consulter le journal d’audit comptable')
) AS permission_seed (code, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = permission_seed.code);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    -- Administration provinciale : tous les droits, y compris le contrôle et la clôture.
    ('ADMIN', 'ACCOUNTING_CHART_READ'),
    ('ADMIN', 'ACCOUNTING_JOURNAL_READ'),
    ('ADMIN', 'ACCOUNTING_JOURNAL_WRITE'),
    ('ADMIN', 'ACCOUNTING_JOURNAL_VALIDATE'),
    ('ADMIN', 'ACCOUNTING_INVOICE_READ'),
    ('ADMIN', 'ACCOUNTING_INVOICE_WRITE'),
    ('ADMIN', 'ACCOUNTING_INVOICE_VALIDATE'),
    ('ADMIN', 'ACCOUNTING_PAYMENT_READ'),
    ('ADMIN', 'ACCOUNTING_PAYMENT_WRITE'),
    ('ADMIN', 'ACCOUNTING_CASH_READ'),
    ('ADMIN', 'ACCOUNTING_CASH_OPEN'),
    ('ADMIN', 'ACCOUNTING_CASH_CLOSE'),
    ('ADMIN', 'ACCOUNTING_REPORT_READ'),
    ('ADMIN', 'ACCOUNTING_ANNEX_READ'),
    ('ADMIN', 'ACCOUNTING_ANNEX_WRITE'),
    ('ADMIN', 'ACCOUNTING_ANNEX_VALIDATE'),
    ('ADMIN', 'ACCOUNTING_PERIOD_CLOSE'),
    ('ADMIN', 'ACCOUNTING_CONFIGURATION_WRITE'),
    ('ADMIN', 'ACCOUNTING_AUDIT_READ'),

    -- Direction d’hôpital : supervision sans saisir ni valider les opérations.
    ('HOSPITAL_ADMIN', 'ACCOUNTING_CHART_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_JOURNAL_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_INVOICE_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_PAYMENT_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_CASH_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_REPORT_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_ANNEX_READ'),
    ('HOSPITAL_ADMIN', 'ACCOUNTING_AUDIT_READ'),

    -- Facturation : préparer les créances, sans encaisser ni valider.
    ('BILLING_OFFICER', 'PATIENT_READ'),
    ('BILLING_OFFICER', 'ACCOUNTING_INVOICE_READ'),
    ('BILLING_OFFICER', 'ACCOUNTING_INVOICE_WRITE'),

    -- Caisse : encaisser les factures existantes et tenir son journal de caisse.
    ('CASHIER', 'PATIENT_READ'),
    ('CASHIER', 'ACCOUNTING_INVOICE_READ'),
    ('CASHIER', 'ACCOUNTING_PAYMENT_READ'),
    ('CASHIER', 'ACCOUNTING_PAYMENT_WRITE'),
    ('CASHIER', 'ACCOUNTING_CASH_READ'),
    ('CASHIER', 'ACCOUNTING_CASH_OPEN'),
    ('CASHIER', 'ACCOUNTING_CASH_CLOSE'),

    -- Comptabilité : préparer les écritures et annexes, sans les auto-valider.
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_CHART_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_JOURNAL_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_JOURNAL_WRITE'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_INVOICE_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_PAYMENT_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_CASH_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_REPORT_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_ANNEX_READ'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_ANNEX_WRITE'),
    ('HOSPITAL_ACCOUNTANT', 'ACCOUNTING_AUDIT_READ'),

    -- Responsable financier : contrôle, validation et clôture, sans caisse.
    ('FINANCE_MANAGER', 'ACCOUNTING_CHART_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_JOURNAL_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_JOURNAL_VALIDATE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_INVOICE_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_INVOICE_WRITE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_INVOICE_VALIDATE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_PAYMENT_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_CASH_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_REPORT_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_ANNEX_READ'),
    ('FINANCE_MANAGER', 'ACCOUNTING_ANNEX_WRITE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_ANNEX_VALIDATE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_PERIOD_CLOSE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_CONFIGURATION_WRITE'),
    ('FINANCE_MANAGER', 'ACCOUNTING_AUDIT_READ'),

    -- Audit indépendant : lecture seule de toutes les pièces de contrôle.
    ('FINANCE_AUDITOR', 'ACCOUNTING_CHART_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_JOURNAL_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_INVOICE_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_PAYMENT_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_CASH_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_REPORT_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_ANNEX_READ'),
    ('FINANCE_AUDITOR', 'ACCOUNTING_AUDIT_READ')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code
LEFT JOIN role_permissions AS role_permission
    ON role_permission.role_id = role.id AND role_permission.permission_id = permission.id
WHERE role_permission.role_id IS NULL;
