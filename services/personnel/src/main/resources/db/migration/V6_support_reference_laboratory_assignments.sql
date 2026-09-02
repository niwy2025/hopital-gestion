-- A reference laboratory is provincial infrastructure, not a hospital unit.
-- Its staff therefore carries the laboratory code without an hospital_id.
ALTER TABLE personnel_assignments DROP CONSTRAINT CK_personnel_assignments_scope;
ALTER TABLE personnel_assignments DROP CONSTRAINT CK_personnel_assignments_access_scope;

ALTER TABLE personnel_assignments ADD CONSTRAINT CK_personnel_assignments_scope
    CHECK (scope IN ('PROVINCIAL', 'HOSPITAL', 'HOSPITAL_LABORATORY', 'REFERENCE_LABORATORY'));

ALTER TABLE personnel_assignments ADD CONSTRAINT CK_personnel_assignments_access_scope CHECK (
    (scope = 'PROVINCIAL' AND hospital_id IS NULL AND laboratory_code IS NULL)
    OR (scope = 'HOSPITAL' AND hospital_id IS NOT NULL AND laboratory_code IS NULL)
    OR (scope = 'HOSPITAL_LABORATORY' AND hospital_id IS NOT NULL AND laboratory_code IS NOT NULL)
    OR (scope = 'REFERENCE_LABORATORY' AND hospital_id IS NULL AND laboratory_code IS NOT NULL)
);
