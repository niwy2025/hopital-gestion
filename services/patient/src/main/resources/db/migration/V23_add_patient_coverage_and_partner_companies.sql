CREATE TABLE partner_companies (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(255),
    address VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by_user_id VARCHAR(100) NOT NULL,
    updated_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_partner_companies PRIMARY KEY (id),
    CONSTRAINT ux_partner_companies_hospital_code UNIQUE (hospital_id, code)
);

CREATE INDEX idx_partner_companies_hospital_name
    ON partner_companies (hospital_id, name);

CREATE TABLE partner_company_members (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    partner_company_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    employee_number VARCHAR(60),
    member_role VARCHAR(20) NOT NULL,
    patient_id UUID,
    sponsor_member_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_by_user_id VARCHAR(100) NOT NULL,
    updated_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_partner_company_members PRIMARY KEY (id),
    CONSTRAINT fk_partner_company_members_company
        FOREIGN KEY (partner_company_id) REFERENCES partner_companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_partner_company_members_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE SET NULL,
    CONSTRAINT fk_partner_company_members_sponsor
        FOREIGN KEY (sponsor_member_id) REFERENCES partner_company_members (id) ON DELETE SET NULL,
    CONSTRAINT ck_partner_company_members_role CHECK (member_role IN ('EMPLOYEE', 'DEPENDENT')),
    CONSTRAINT ck_partner_company_members_sponsor CHECK (
        (member_role = 'EMPLOYEE' AND sponsor_member_id IS NULL)
        OR (member_role = 'DEPENDENT' AND sponsor_member_id IS NOT NULL)
    )
);

CREATE INDEX idx_partner_company_members_company_name
    ON partner_company_members (partner_company_id, full_name);

CREATE INDEX idx_partner_company_members_patient
    ON partner_company_members (patient_id);

ALTER TABLE patients
    ADD COLUMN coverage_type VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    ADD COLUMN partner_company_id UUID,
    ADD COLUMN partner_company_member_id UUID;

ALTER TABLE patients
    ADD CONSTRAINT ck_patients_coverage_type CHECK (coverage_type IN ('PRIVATE', 'CONVENTION')),
    ADD CONSTRAINT ck_patients_coverage_refs CHECK (
        (coverage_type = 'PRIVATE' AND partner_company_id IS NULL AND partner_company_member_id IS NULL)
        OR (coverage_type = 'CONVENTION' AND partner_company_id IS NOT NULL AND partner_company_member_id IS NOT NULL)
    ),
    ADD CONSTRAINT fk_patients_partner_company
        FOREIGN KEY (partner_company_id) REFERENCES partner_companies (id),
    ADD CONSTRAINT fk_patients_partner_company_member
        FOREIGN KEY (partner_company_member_id) REFERENCES partner_company_members (id);

CREATE INDEX idx_patients_partner_company
    ON patients (partner_company_id);

ALTER TABLE patient_documents
    DROP CONSTRAINT ck_patient_documents_type;

ALTER TABLE patient_documents
    ADD CONSTRAINT ck_patient_documents_type CHECK (document_type IN (
        'PROFILE_PHOTO', 'IDENTITY_CARD', 'PASSPORT', 'BIRTH_CERTIFICATE',
        'HEALTH_INSURANCE', 'REFERRAL_LETTER', 'CONVENTION_CARNET', 'OTHER'
    ));
