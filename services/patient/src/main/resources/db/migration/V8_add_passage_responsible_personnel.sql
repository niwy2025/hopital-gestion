ALTER TABLE patient_passages
    ADD COLUMN responsible_personnel_id UUID,
    ADD COLUMN responsible_personnel_employee_number VARCHAR(40),
    ADD COLUMN responsible_personnel_name VARCHAR(250),
    ADD COLUMN responsible_personnel_job_title VARCHAR(150),
    ADD COLUMN responsible_assigned_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN responsible_assigned_by_user_id VARCHAR(100),
    ADD COLUMN responsible_assigned_by_username VARCHAR(150);

CREATE INDEX idx_patient_passages_responsible_personnel
    ON patient_passages (responsible_personnel_id)
    WHERE responsible_personnel_id IS NOT NULL;
