CREATE TABLE patient_passage_triage_assessments (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    passage_id UUID NOT NULL,
    chief_complaint VARCHAR(500) NOT NULL,
    injury BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(10) NOT NULL,
    priority_reason VARCHAR(1000) NOT NULL,
    heart_rate_bpm INTEGER,
    respiratory_rate_per_minute INTEGER,
    systolic_blood_pressure INTEGER,
    diastolic_blood_pressure INTEGER,
    temperature_celsius NUMERIC(5, 2),
    oxygen_saturation_percent INTEGER,
    random_blood_glucose_mg_dl INTEGER,
    pain_score INTEGER,
    weight_kg NUMERIC(6, 2),
    consciousness_level VARCHAR(20) NOT NULL,
    pregnancy_status VARCHAR(20) NOT NULL,
    capillary_refill_seconds INTEGER,
    oxygen_support VARCHAR(200),
    oxygen_flow_lpm NUMERIC(5, 2),
    care_on_arrival TEXT,
    handover_notes TEXT,
    recorded_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    recorded_by_user_id VARCHAR(100) NOT NULL,
    recorded_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_passage_triage_assessments PRIMARY KEY (id),
    CONSTRAINT fk_patient_passage_triage_assessments_passage
        FOREIGN KEY (passage_id) REFERENCES patient_passages (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_passage_triage_priority CHECK (priority IN ('RED', 'YELLOW', 'GREEN')),
    CONSTRAINT ck_patient_passage_triage_consciousness CHECK (consciousness_level IN (
        'ALERT', 'VERBAL', 'PAIN', 'UNRESPONSIVE', 'UNKNOWN'
    )),
    CONSTRAINT ck_patient_passage_triage_pregnancy CHECK (pregnancy_status IN (
        'YES', 'NO', 'UNKNOWN', 'NOT_APPLICABLE'
    )),
    CONSTRAINT ck_patient_passage_triage_pain CHECK (pain_score IS NULL OR pain_score BETWEEN 0 AND 10),
    CONSTRAINT ck_patient_passage_triage_oxygen_saturation CHECK (
        oxygen_saturation_percent IS NULL OR oxygen_saturation_percent BETWEEN 0 AND 100
    )
);

CREATE INDEX idx_patient_passage_triage_assessments_passage_recorded_at
    ON patient_passage_triage_assessments (passage_id, recorded_at DESC);

CREATE INDEX idx_patient_passage_triage_assessments_passage_priority
    ON patient_passage_triage_assessments (passage_id, priority);

CREATE TABLE patient_passage_triage_danger_signs (
    triage_assessment_id UUID NOT NULL,
    danger_sign VARCHAR(50) NOT NULL,
    CONSTRAINT pk_patient_passage_triage_danger_signs PRIMARY KEY (triage_assessment_id, danger_sign),
    CONSTRAINT fk_patient_passage_triage_danger_signs_assessment
        FOREIGN KEY (triage_assessment_id) REFERENCES patient_passage_triage_assessments (id) ON DELETE CASCADE,
    CONSTRAINT ck_patient_passage_triage_danger_sign CHECK (danger_sign IN (
        'RESPIRATORY_DISTRESS', 'CENTRAL_CYANOSIS', 'WEAK_FAST_PULSE', 'HEAVY_BLEEDING',
        'ALTERED_MENTAL_STATUS', 'ACUTE_CONVULSION', 'SEVERE_PAIN', 'HIGH_RISK_TRAUMA',
        'POISONING_OR_CHEMICAL_EXPOSURE', 'THREATENED_LIMB'
    ))
);
