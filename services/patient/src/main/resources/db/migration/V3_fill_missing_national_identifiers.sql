-- Les dossiers historiques sans identifiant national reçoivent une référence
-- générique et stable. Les nouvelles références sont générées par le service.
UPDATE patients
SET national_identifier = 'NAT-LEGACY-' || UPPER(REPLACE(id::text, '-', ''))
WHERE national_identifier IS NULL;

ALTER TABLE patients
    ALTER COLUMN national_identifier SET NOT NULL;
