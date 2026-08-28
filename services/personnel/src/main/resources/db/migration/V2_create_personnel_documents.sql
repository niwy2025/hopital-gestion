CREATE TABLE personnel_documents (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    personnel_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes INT NOT NULL,
    content_base64 TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_personnel_documents PRIMARY KEY (id),
    CONSTRAINT FK_personnel_documents_personnel FOREIGN KEY (personnel_id) REFERENCES personnel(id) ON DELETE CASCADE,
    CONSTRAINT CK_personnel_documents_size CHECK (size_bytes > 0),
    CONSTRAINT CK_personnel_documents_type CHECK (document_type IN (
        'PROFILE_PHOTO', 'SIGNATURE', 'CV', 'IDENTITY_DOCUMENT', 'DIPLOMA',
        'PROFESSIONAL_LICENSE', 'CONTRACT', 'OTHER'
    ))
);

CREATE INDEX IX_personnel_documents_personnel_created_at
    ON personnel_documents (personnel_id, created_at DESC);

CREATE UNIQUE INDEX UX_personnel_documents_profile_photo
    ON personnel_documents (personnel_id)
    WHERE document_type = 'PROFILE_PHOTO';

CREATE UNIQUE INDEX UX_personnel_documents_signature
    ON personnel_documents (personnel_id)
    WHERE document_type = 'SIGNATURE';

CREATE UNIQUE INDEX UX_personnel_documents_cv
    ON personnel_documents (personnel_id)
    WHERE document_type = 'CV';
