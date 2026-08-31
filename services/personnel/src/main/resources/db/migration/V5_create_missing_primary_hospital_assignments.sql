-- Les premières fiches créées avant l'automatisation pouvaient avoir un hôpital
-- sans affectation principale. Cette affectation est nécessaire pour déterminer
-- le périmètre réel d'un compte lors de la connexion.
INSERT INTO personnel_assignments (
    id,
    personnel_id,
    scope,
    hospital_id,
    laboratory_code,
    department_name,
    unit_name,
    position_title,
    starts_on,
    ends_on,
    status,
    primary_assignment,
    notes,
    created_at
)
SELECT
    gen_random_uuid(),
    personnel.id,
    'HOSPITAL',
    personnel.hospital_id,
    NULL,
    NULL,
    NULL,
    personnel.job_title,
    CURRENT_DATE,
    NULL,
    'ACTIVE',
    TRUE,
    'Affectation initiale créée automatiquement.',
    CURRENT_TIMESTAMP
FROM personnel
WHERE personnel.active = TRUE
  AND personnel.hospital_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM personnel_assignments assignment
      WHERE assignment.personnel_id = personnel.id
        AND assignment.status = 'ACTIVE'
        AND assignment.primary_assignment = TRUE
  );
