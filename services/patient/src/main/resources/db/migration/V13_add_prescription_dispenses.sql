CREATE TABLE patient_passage_prescription_dispenses (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    prescription_id UUID NOT NULL,
    completion VARCHAR(20) NOT NULL,
    notes TEXT,
    dispensed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    dispensed_by_user_id VARCHAR(100) NOT NULL,
    dispensed_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_passage_prescription_dispenses PRIMARY KEY (id),
    CONSTRAINT uk_patient_passage_prescription_dispenses_code UNIQUE (code),
    CONSTRAINT fk_patient_passage_prescription_dispenses_prescription
        FOREIGN KEY (prescription_id) REFERENCES patient_passage_prescriptions (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_passage_prescription_dispenses_completion
        CHECK (completion IN ('PARTIAL', 'COMPLETE'))
);

CREATE INDEX idx_patient_passage_prescription_dispenses_prescription_date
    ON patient_passage_prescription_dispenses (prescription_id, dispensed_at DESC);

CREATE TABLE patient_passage_prescription_dispense_items (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    dispense_id UUID NOT NULL,
    prescription_item_id UUID NOT NULL,
    dispensed_quantity VARCHAR(100) NOT NULL,
    CONSTRAINT pk_patient_passage_prescription_dispense_items PRIMARY KEY (id),
    CONSTRAINT fk_patient_passage_prescription_dispense_items_dispense
        FOREIGN KEY (dispense_id) REFERENCES patient_passage_prescription_dispenses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_patient_passage_prescription_dispense_items_prescription_item
        FOREIGN KEY (prescription_item_id) REFERENCES patient_passage_prescription_items (id) ON DELETE RESTRICT,
    CONSTRAINT uk_patient_passage_prescription_dispense_items_item
        UNIQUE (dispense_id, prescription_item_id)
);

CREATE INDEX idx_patient_passage_prescription_dispense_items_dispense
    ON patient_passage_prescription_dispense_items (dispense_id);
