CREATE TABLE personnel_assignments (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_personnel_assignments_id DEFAULT NEWID(),
    personnel_id UNIQUEIDENTIFIER NOT NULL,
    scope NVARCHAR(20) NOT NULL,
    hospital_id UNIQUEIDENTIFIER NULL,
    department_name NVARCHAR(150) NULL,
    unit_name NVARCHAR(150) NULL,
    position_title NVARCHAR(150) NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NULL,
    status NVARCHAR(20) NOT NULL,
    primary_assignment BIT NOT NULL CONSTRAINT DF_personnel_assignments_primary DEFAULT 0,
    notes NVARCHAR(1000) NULL,
    created_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT PK_personnel_assignments PRIMARY KEY (id),
    CONSTRAINT FK_personnel_assignments_personnel FOREIGN KEY (personnel_id) REFERENCES personnel(id) ON DELETE CASCADE,
    CONSTRAINT CK_personnel_assignments_scope CHECK (scope IN ('PROVINCIAL', 'HOSPITAL')),
    CONSTRAINT CK_personnel_assignments_hospital CHECK (
        (scope = 'PROVINCIAL' AND hospital_id IS NULL) OR (scope = 'HOSPITAL' AND hospital_id IS NOT NULL)
    ),
    CONSTRAINT CK_personnel_assignments_status CHECK (status IN ('ACTIVE', 'ENDED')),
    CONSTRAINT CK_personnel_assignments_dates CHECK (ends_on IS NULL OR ends_on >= starts_on)
);

CREATE INDEX IX_personnel_assignments_personnel_starts_on
    ON personnel_assignments (personnel_id, starts_on DESC);

CREATE UNIQUE INDEX UX_personnel_assignments_active_primary
    ON personnel_assignments (personnel_id)
    WHERE primary_assignment = 1 AND status = 'ACTIVE';
