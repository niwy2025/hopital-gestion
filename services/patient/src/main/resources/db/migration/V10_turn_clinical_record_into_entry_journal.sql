-- La première version ne conservait qu'une synthèse clinique par passage.
-- Chaque synthèse existante devient ici une première entrée historique.
ALTER TABLE patient_passage_clinical_records
    RENAME TO patient_passage_clinical_entries;

ALTER TABLE patient_passage_clinical_entries
    DROP CONSTRAINT uk_patient_passage_clinical_records_passage;

ALTER TABLE patient_passage_clinical_entries
    ADD COLUMN entry_type VARCHAR(30) NOT NULL DEFAULT 'INITIAL_ASSESSMENT';

ALTER TABLE patient_passage_clinical_entries
    RENAME COLUMN updated_at TO recorded_at;

ALTER TABLE patient_passage_clinical_entries
    RENAME COLUMN updated_by_user_id TO recorded_by_user_id;

ALTER TABLE patient_passage_clinical_entries
    RENAME COLUMN updated_by_username TO recorded_by_username;

-- Le contenu encore présent est celui de la dernière mise à jour de l'ancienne
-- synthèse : son auteur et sa date deviennent donc les références de l'entrée.
ALTER TABLE patient_passage_clinical_entries
    DROP COLUMN created_at,
    DROP COLUMN created_by_user_id,
    DROP COLUMN created_by_username;

ALTER INDEX idx_patient_passage_clinical_records_updated_at
    RENAME TO idx_patient_passage_clinical_entries_recorded_at;

CREATE INDEX idx_patient_passage_clinical_entries_passage_recorded_at
    ON patient_passage_clinical_entries (passage_id, recorded_at DESC);

ALTER TABLE patient_passage_clinical_entries
    ADD CONSTRAINT ck_patient_passage_clinical_entries_type CHECK (entry_type IN (
        'INITIAL_ASSESSMENT', 'CLINICAL_EVOLUTION', 'DISCHARGE_NOTE'
    ));
