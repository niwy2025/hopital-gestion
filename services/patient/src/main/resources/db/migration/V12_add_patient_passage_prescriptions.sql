CREATE TABLE patient_passage_prescriptions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    passage_id UUID NOT NULL,
    source VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_prescriber_name VARCHAR(200),
    external_reference VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_passage_prescriptions PRIMARY KEY (id),
    CONSTRAINT uk_patient_passage_prescriptions_code UNIQUE (code),
    CONSTRAINT fk_patient_passage_prescriptions_passage
        FOREIGN KEY (passage_id) REFERENCES patient_passages (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_passage_prescriptions_source CHECK (source IN ('MEDICAL', 'EXTERNAL_PAPER')),
    CONSTRAINT ck_patient_passage_prescriptions_status CHECK (status IN ('PENDING_DISPENSING', 'PARTIALLY_DISPENSED', 'DISPENSED', 'CANCELLED'))
);

CREATE INDEX idx_patient_passage_prescriptions_passage_created_at
    ON patient_passage_prescriptions (passage_id, created_at DESC);

CREATE TABLE patient_passage_prescription_items (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    prescription_id UUID NOT NULL,
    medicine_name VARCHAR(250) NOT NULL,
    dosage VARCHAR(150),
    administration_route VARCHAR(100),
    frequency VARCHAR(150),
    duration VARCHAR(150),
    quantity VARCHAR(100),
    instructions TEXT,
    display_order INTEGER NOT NULL,
    CONSTRAINT pk_patient_passage_prescription_items PRIMARY KEY (id),
    CONSTRAINT fk_patient_passage_prescription_items_prescription
        FOREIGN KEY (prescription_id) REFERENCES patient_passage_prescriptions (id) ON DELETE RESTRICT,
    CONSTRAINT uk_patient_passage_prescription_items_order UNIQUE (prescription_id, display_order)
);

CREATE INDEX idx_patient_passage_prescription_items_prescription
    ON patient_passage_prescription_items (prescription_id, display_order);
