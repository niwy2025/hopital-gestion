CREATE TABLE patient_passage_responsibility_steps (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    passage_id UUID NOT NULL,
    step_kind VARCHAR(40) NOT NULL,
    step_label VARCHAR(150) NOT NULL,
    related_entity_id UUID,
    responsible_personnel_id UUID NOT NULL,
    responsible_personnel_employee_number VARCHAR(40),
    responsible_personnel_name VARCHAR(250) NOT NULL,
    responsible_personnel_job_title VARCHAR(150),
    assigned_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    assigned_by_user_id VARCHAR(100) NOT NULL,
    assigned_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT pk_patient_passage_responsibility_steps PRIMARY KEY (id),
    CONSTRAINT fk_passage_responsibility_steps_passage
        FOREIGN KEY (passage_id) REFERENCES patient_passages (id) ON DELETE CASCADE,
    CONSTRAINT ck_passage_responsibility_steps_kind CHECK (step_kind IN (
        'ASSIGNMENT',
        'CLINICAL_ENTRY',
        'TRIAGE',
        'PRESCRIPTION',
        'LABORATORY_REQUEST',
        'SERVICE_TRANSFER'
    ))
);

CREATE INDEX idx_passage_responsibility_steps_passage_assigned_at
    ON patient_passage_responsibility_steps (passage_id, assigned_at DESC);

INSERT INTO patient_passage_responsibility_steps (
    id,
    passage_id,
    step_kind,
    step_label,
    related_entity_id,
    responsible_personnel_id,
    responsible_personnel_employee_number,
    responsible_personnel_name,
    responsible_personnel_job_title,
    assigned_at,
    assigned_by_user_id,
    assigned_by_username
)
SELECT
    gen_random_uuid(),
    passage.id,
    'ASSIGNMENT',
    'Responsable initial du passage',
    NULL,
    passage.responsible_personnel_id,
    passage.responsible_personnel_employee_number,
    COALESCE(passage.responsible_personnel_name, 'Personnel responsable'),
    passage.responsible_personnel_job_title,
    COALESCE(passage.responsible_assigned_at, passage.arrived_at),
    COALESCE(passage.responsible_assigned_by_user_id, 'migration'),
    COALESCE(passage.responsible_assigned_by_username, 'Migration')
FROM patient_passages passage
WHERE passage.responsible_personnel_id IS NOT NULL;
