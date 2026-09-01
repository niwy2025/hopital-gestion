CREATE TABLE patient_documents (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes INT NOT NULL,
    content_base64 TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_documents PRIMARY KEY (id),
    CONSTRAINT fk_patient_documents_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE,
    CONSTRAINT ck_patient_documents_size CHECK (size_bytes > 0),
    CONSTRAINT ck_patient_documents_type CHECK (document_type IN (
        'PROFILE_PHOTO', 'IDENTITY_CARD', 'PASSPORT', 'BIRTH_CERTIFICATE',
        'HEALTH_INSURANCE', 'REFERRAL_LETTER', 'OTHER'
    ))
);

CREATE INDEX idx_patient_documents_patient_created_at
    ON patient_documents (patient_id, created_at DESC);

CREATE UNIQUE INDEX ux_patient_documents_profile_photo
    ON patient_documents (patient_id)
    WHERE document_type = 'PROFILE_PHOTO';
