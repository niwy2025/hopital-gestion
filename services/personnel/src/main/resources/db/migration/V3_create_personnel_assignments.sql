CREATE TABLE personnel_assignments (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    personnel_id UUID NOT NULL,
    scope VARCHAR(20) NOT NULL,
    hospital_id UUID,
    department_name VARCHAR(150),
    unit_name VARCHAR(150),
    position_title VARCHAR(150) NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE,
    status VARCHAR(20) NOT NULL,
    primary_assignment BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(1000),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
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
    WHERE primary_assignment = TRUE AND status = 'ACTIVE';
