CREATE TABLE patient_passages (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    patient_id UUID NOT NULL,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    type VARCHAR(30) NOT NULL,
    service_name VARCHAR(150),
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    arrived_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP(6) WITH TIME ZONE,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    closed_by_user_id VARCHAR(100),
    closed_by_username VARCHAR(150),
    CONSTRAINT pk_patient_passages PRIMARY KEY (id),
    CONSTRAINT uk_patient_passages_code UNIQUE (code),
    CONSTRAINT fk_patient_passages_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE RESTRICT
);

CREATE INDEX idx_patient_passages_patient_arrived_at
    ON patient_passages (patient_id, arrived_at DESC);

CREATE INDEX idx_patient_passages_patient_status
    ON patient_passages (patient_id, status);
