ALTER TABLE patients
    ADD COLUMN middle_name VARCHAR(100),
    ADD COLUMN national_identifier VARCHAR(100),
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN emergency_contact_name VARCHAR(200),
    ADD COLUMN emergency_contact_phone VARCHAR(30),
    ADD COLUMN emergency_contact_relationship VARCHAR(100),
    ADD COLUMN registration_hospital_id UUID;

CREATE UNIQUE INDEX uk_patients_national_identifier
    ON patients (national_identifier)
    WHERE national_identifier IS NOT NULL;

CREATE INDEX idx_patients_registration_hospital_id
    ON patients (registration_hospital_id);

CREATE INDEX idx_patients_identity
    ON patients (last_name, first_name, date_of_birth, gender);
