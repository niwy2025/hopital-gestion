CREATE TABLE patient_passage_clinical_records (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    passage_id UUID NOT NULL,
    clinical_findings TEXT NOT NULL,
    diagnosis TEXT,
    care_plan TEXT,
    orientation VARCHAR(30) NOT NULL,
    follow_up_on DATE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by_user_id VARCHAR(100) NOT NULL,
    updated_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_passage_clinical_records PRIMARY KEY (id),
    CONSTRAINT uk_patient_passage_clinical_records_passage UNIQUE (passage_id),
    CONSTRAINT fk_patient_passage_clinical_records_passage
        FOREIGN KEY (passage_id) REFERENCES patient_passages (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_passage_clinical_records_orientation CHECK (orientation IN (
        'OBSERVATION', 'HOSPITALIZATION', 'REFERRAL', 'LABORATORY',
        'PHARMACY', 'FOLLOW_UP', 'DISCHARGE', 'OTHER'
    ))
);

CREATE INDEX idx_patient_passage_clinical_records_updated_at
    ON patient_passage_clinical_records (updated_at DESC);
