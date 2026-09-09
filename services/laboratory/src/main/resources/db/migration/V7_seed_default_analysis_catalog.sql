-- Garantit qu'une installation existante possède au moins une analyse
-- structurée active. Cette migration est idempotente vis-à-vis du code métier
-- HEMOGRAM et ne remplace pas les paramètres déjà personnalisés.
INSERT INTO analysis_definitions (
    id,
    code,
    name,
    description,
    specimen_type,
    active,
    created_at,
    created_by
)
VALUES (
    gen_random_uuid(),
    'HEMOGRAM',
    'Hémogramme (NFS)',
    'Numération formule sanguine réalisée sur sang total.',
    'BLOOD',
    TRUE,
    CURRENT_TIMESTAMP,
    'migration-v7'
)
ON CONFLICT (code) DO UPDATE
SET active = TRUE;

INSERT INTO analysis_definition_parameters (
    id,
    analysis_definition_id,
    code,
    name,
    value_type,
    unit,
    reference_range,
    qualitative_options,
    required,
    display_order
)
SELECT
    gen_random_uuid(),
    definition.id,
    parameter.code,
    parameter.name,
    parameter.value_type,
    parameter.unit,
    parameter.reference_range,
    NULL,
    TRUE,
    parameter.display_order
FROM analysis_definitions definition
CROSS JOIN (
    VALUES
        ('HEMOGLOBIN', 'Hémoglobine', 'DECIMAL', 'g/dL', 'Selon la méthode du laboratoire, l’âge et le sexe', 1),
        ('HEMATOCRIT', 'Hématocrite', 'PERCENTAGE', '%', 'Selon la méthode du laboratoire, l’âge et le sexe', 2),
        ('LEUKOCYTES', 'Leucocytes', 'DECIMAL', 'G/L', 'Selon la méthode du laboratoire et l’âge', 3),
        ('PLATELETS', 'Plaquettes', 'INTEGER', 'G/L', 'Selon la méthode du laboratoire et l’âge', 4)
) AS parameter(code, name, value_type, unit, reference_range, display_order)
WHERE definition.code = 'HEMOGRAM'
ON CONFLICT (analysis_definition_id, code) DO NOTHING;
