ALTER TABLE partner_company_members
    DROP CONSTRAINT ck_partner_company_members_sponsor;

ALTER TABLE partner_company_members
    ADD COLUMN dependent_relationship VARCHAR(20);

UPDATE partner_company_members
SET dependent_relationship = 'OTHER'
WHERE member_role = 'DEPENDENT'
  AND dependent_relationship IS NULL;

ALTER TABLE partner_company_members
    ADD CONSTRAINT ck_partner_company_members_sponsor CHECK (
        (member_role = 'EMPLOYEE' AND sponsor_member_id IS NULL AND dependent_relationship IS NULL)
        OR (
            member_role = 'DEPENDENT'
            AND sponsor_member_id IS NOT NULL
            AND dependent_relationship IN ('SPOUSE', 'CHILD', 'PARENT', 'OTHER')
        )
    );

CREATE TABLE partner_company_documents (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    partner_company_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes INT NOT NULL,
    content_base64 TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_partner_company_documents PRIMARY KEY (id),
    CONSTRAINT fk_partner_company_documents_company
        FOREIGN KEY (partner_company_id) REFERENCES partner_companies (id) ON DELETE CASCADE,
    CONSTRAINT ck_partner_company_documents_size CHECK (size_bytes > 0),
    CONSTRAINT ck_partner_company_documents_type CHECK (document_type IN ('CARNET', 'OTHER'))
);

CREATE INDEX idx_partner_company_documents_company_created_at
    ON partner_company_documents (partner_company_id, created_at DESC);
