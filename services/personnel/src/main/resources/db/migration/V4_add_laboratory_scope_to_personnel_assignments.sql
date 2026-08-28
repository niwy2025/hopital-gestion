ALTER TABLE personnel_assignments ADD laboratory_code VARCHAR(30) NULL;

ALTER TABLE personnel_assignments DROP CONSTRAINT CK_personnel_assignments_scope;
ALTER TABLE personnel_assignments DROP CONSTRAINT CK_personnel_assignments_hospital;

ALTER TABLE personnel_assignments ADD CONSTRAINT CK_personnel_assignments_scope
    CHECK (scope IN ('PROVINCIAL', 'HOSPITAL', 'HOSPITAL_LABORATORY'));

ALTER TABLE personnel_assignments ADD CONSTRAINT CK_personnel_assignments_access_scope CHECK (
    (scope = 'PROVINCIAL' AND hospital_id IS NULL AND laboratory_code IS NULL)
    OR (scope = 'HOSPITAL' AND hospital_id IS NOT NULL AND laboratory_code IS NULL)
    OR (scope = 'HOSPITAL_LABORATORY' AND hospital_id IS NOT NULL AND laboratory_code IS NOT NULL)
);

CREATE INDEX IX_personnel_assignments_active_laboratory
    ON personnel_assignments (laboratory_code)
    WHERE status = 'ACTIVE' AND laboratory_code IS NOT NULL;
