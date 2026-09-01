-- Une demande peut aussi appartenir à un épisode de prise en charge. La base
-- Patient restant indépendante, il s'agit d'une référence UUID sans clé
-- étrangère inter-base.
ALTER TABLE analysis_requests
    ADD COLUMN patient_passage_id UUID;

CREATE INDEX idx_analysis_requests_patient_passage_created_at
    ON analysis_requests (patient_passage_id, created_at DESC);
