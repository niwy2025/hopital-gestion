ALTER TABLE patients
    ADD COLUMN created_by_user_id VARCHAR(100),
    ADD COLUMN created_by_username VARCHAR(150),
    ADD COLUMN updated_by_user_id VARCHAR(100),
    ADD COLUMN updated_by_username VARCHAR(150),
    ADD COLUMN updated_at TIMESTAMP(6) WITH TIME ZONE;

-- Les dossiers antérieurs à cette migration ne permettent pas de retrouver
-- de façon fiable leur opérateur. Ils sont explicitement marqués comme tels.
UPDATE patients
SET created_by_user_id = 'SYSTEM_HISTORY',
    created_by_username = 'Historique (opérateur inconnu)',
    updated_by_user_id = 'SYSTEM_HISTORY',
    updated_by_username = 'Historique (opérateur inconnu)',
    updated_at = created_at;

ALTER TABLE patients
    ALTER COLUMN created_by_user_id SET NOT NULL,
    ALTER COLUMN created_by_username SET NOT NULL,
    ALTER COLUMN updated_by_user_id SET NOT NULL,
    ALTER COLUMN updated_by_username SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE TABLE patient_audit_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    operator_user_id VARCHAR(100) NOT NULL,
    operator_username VARCHAR(150) NOT NULL,
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_patient_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_patient_audit_events_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE
);

CREATE INDEX idx_patient_audit_events_patient_occurred_at
    ON patient_audit_events (patient_id, occurred_at DESC);

INSERT INTO patient_audit_events (
    id,
    patient_id,
    event_type,
    description,
    operator_user_id,
    operator_username,
    occurred_at
)
SELECT
    gen_random_uuid(),
    id,
    'IMPORTED',
    'Dossier créé avant l’activation de la traçabilité.',
    created_by_user_id,
    created_by_username,
    created_at
FROM patients;
