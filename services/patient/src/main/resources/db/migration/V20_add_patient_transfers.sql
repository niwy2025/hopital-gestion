CREATE TABLE patient_transfers (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    source_passage_id UUID NOT NULL REFERENCES patient_passages(id),
    destination_passage_id UUID UNIQUE REFERENCES patient_passages(id),
    destination_hospital_id UUID,
    destination_hospital_code VARCHAR(30),
    source_hospital_name VARCHAR(200),
    destination_hospital_name VARCHAR(200),
    external_facility_name VARCHAR(200),
    external_facility_address VARCHAR(500),
    destination_contact VARCHAR(100),
    patient_name VARCHAR(310) NOT NULL,
    patient_date_of_birth DATE NOT NULL,
    patient_gender VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    clinical_summary VARCHAR(8000) NOT NULL,
    treatment_and_instructions VARCHAR(4000),
    transport_details VARCHAR(1000),
    urgent BOOLEAN NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    requested_by_user_id VARCHAR(100) NOT NULL,
    requested_by_username VARCHAR(150) NOT NULL,
    dispatched_at TIMESTAMPTZ,
    dispatched_by_user_id VARCHAR(100),
    dispatched_by_username VARCHAR(150),
    resolved_at TIMESTAMPTZ,
    resolved_by_user_id VARCHAR(100),
    resolved_by_username VARCHAR(150),
    resolution_note VARCHAR(1000),
    CONSTRAINT ck_patient_transfer_destination CHECK (
        (destination_hospital_id IS NOT NULL AND destination_hospital_code IS NOT NULL AND external_facility_name IS NULL)
        OR (destination_hospital_id IS NULL AND destination_hospital_code IS NULL AND external_facility_name IS NOT NULL)
    ),
    CONSTRAINT ck_patient_transfer_status CHECK (
        status IN ('REQUESTED', 'IN_TRANSIT', 'RECEIVED', 'EXTERNAL_COMPLETED', 'CANCELLED')
    )
);
CREATE UNIQUE INDEX uk_patient_transfer_active_source ON patient_transfers(source_passage_id)
    WHERE status <> 'CANCELLED';
CREATE INDEX idx_patient_transfers_destination_status ON patient_transfers(destination_hospital_id, status, requested_at DESC);
CREATE INDEX idx_patient_passages_hospital_patient ON patient_passages(hospital_code, patient_id);
