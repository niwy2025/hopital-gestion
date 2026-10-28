-- Catalogue hospitalier : mesures / signes vitaux supplémentaires et signes d'alerte.
-- Les constantes système (colonnes de patient_passage_triage_assessments) restent
-- disponibles pour tous les établissements.

CREATE TABLE hospital_triage_measurements (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL,
    code VARCHAR(40) NOT NULL,
    label VARCHAR(150) NOT NULL,
    unit VARCHAR(40),
    value_kind VARCHAR(20) NOT NULL,
    min_value NUMERIC(12, 4),
    max_value NUMERIC(12, 4),
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_by_user_id VARCHAR(100),
    updated_by_username VARCHAR(150),
    CONSTRAINT pk_hospital_triage_measurements PRIMARY KEY (id),
    CONSTRAINT uk_hospital_triage_measurements_hospital_code UNIQUE (hospital_id, code),
    CONSTRAINT ck_hospital_triage_measurements_value_kind CHECK (value_kind IN ('NUMBER', 'TEXT')),
    CONSTRAINT ck_hospital_triage_measurements_code CHECK (code ~ '^[A-Z0-9_]{2,40}$')
);

CREATE INDEX idx_hospital_triage_measurements_hospital_active_order
    ON hospital_triage_measurements (hospital_id, active, display_order, label);

CREATE TABLE hospital_triage_alert_signs (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL,
    code VARCHAR(40) NOT NULL,
    label VARCHAR(150) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_by_user_id VARCHAR(100),
    updated_by_username VARCHAR(150),
    CONSTRAINT pk_hospital_triage_alert_signs PRIMARY KEY (id),
    CONSTRAINT uk_hospital_triage_alert_signs_hospital_code UNIQUE (hospital_id, code),
    CONSTRAINT ck_hospital_triage_alert_signs_code CHECK (code ~ '^[A-Z0-9_]{2,40}$')
);

CREATE INDEX idx_hospital_triage_alert_signs_hospital_active_order
    ON hospital_triage_alert_signs (hospital_id, active, display_order, label);

CREATE TABLE patient_passage_triage_custom_values (
    triage_assessment_id UUID NOT NULL,
    measurement_id UUID NOT NULL,
    numeric_value NUMERIC(12, 4),
    text_value VARCHAR(500),
    CONSTRAINT pk_patient_passage_triage_custom_values PRIMARY KEY (triage_assessment_id, measurement_id),
    CONSTRAINT fk_triage_custom_values_assessment
        FOREIGN KEY (triage_assessment_id) REFERENCES patient_passage_triage_assessments (id) ON DELETE CASCADE,
    CONSTRAINT fk_triage_custom_values_measurement
        FOREIGN KEY (measurement_id) REFERENCES hospital_triage_measurements (id) ON DELETE RESTRICT,
    CONSTRAINT ck_triage_custom_values_has_value CHECK (
        numeric_value IS NOT NULL OR (text_value IS NOT NULL AND btrim(text_value) <> '')
    )
);

CREATE INDEX idx_triage_custom_values_measurement
    ON patient_passage_triage_custom_values (measurement_id);

CREATE TABLE patient_passage_triage_custom_alert_signs (
    triage_assessment_id UUID NOT NULL,
    alert_sign_id UUID NOT NULL,
    CONSTRAINT pk_patient_passage_triage_custom_alert_signs PRIMARY KEY (triage_assessment_id, alert_sign_id),
    CONSTRAINT fk_triage_custom_alert_assessment
        FOREIGN KEY (triage_assessment_id) REFERENCES patient_passage_triage_assessments (id) ON DELETE CASCADE,
    CONSTRAINT fk_triage_custom_alert_sign
        FOREIGN KEY (alert_sign_id) REFERENCES hospital_triage_alert_signs (id) ON DELETE RESTRICT
);

CREATE INDEX idx_triage_custom_alert_signs_alert
    ON patient_passage_triage_custom_alert_signs (alert_sign_id);
