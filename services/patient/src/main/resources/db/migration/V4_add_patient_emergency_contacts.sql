CREATE TABLE patient_emergency_contacts (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    relationship VARCHAR(30) NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT pk_patient_emergency_contacts PRIMARY KEY (id),
    CONSTRAINT fk_patient_emergency_contacts_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE,
    CONSTRAINT uk_patient_emergency_contacts_order UNIQUE (patient_id, display_order)
);

CREATE INDEX idx_patient_emergency_contacts_patient_id
    ON patient_emergency_contacts (patient_id);

-- Préserver le contact historique éventuel lors de la transition vers une liste.
INSERT INTO patient_emergency_contacts (id, patient_id, full_name, phone_number, relationship, display_order)
SELECT
    gen_random_uuid(),
    id,
    COALESCE(NULLIF(TRIM(emergency_contact_name), ''), 'Contact non renseigné'),
    COALESCE(NULLIF(TRIM(emergency_contact_phone), ''), 'Non renseigné'),
    'OTHER',
    0
FROM patients
WHERE NULLIF(TRIM(COALESCE(emergency_contact_name, '')), '') IS NOT NULL
   OR NULLIF(TRIM(COALESCE(emergency_contact_phone, '')), '') IS NOT NULL
   OR NULLIF(TRIM(COALESCE(emergency_contact_relationship, '')), '') IS NOT NULL;
