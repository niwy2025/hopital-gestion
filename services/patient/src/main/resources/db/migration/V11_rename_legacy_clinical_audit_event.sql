-- Les premières versions du suivi clinique enregistraient cette valeur dans
-- l'audit. Le journal est désormais append-only : chaque ancienne mise à jour
-- est donc conservée comme une entrée clinique ajoutée.
UPDATE patient_audit_events
SET type = 'CLINICAL_ENTRY_ADDED'
WHERE type = 'CLINICAL_RECORD_UPDATED';
