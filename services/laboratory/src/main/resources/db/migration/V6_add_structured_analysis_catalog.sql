CREATE TABLE analysis_definitions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    specimen_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(100),
    CONSTRAINT pk_analysis_definitions PRIMARY KEY (id),
    CONSTRAINT uk_analysis_definitions_code UNIQUE (code)
);

CREATE TABLE analysis_definition_parameters (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    analysis_definition_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    value_type VARCHAR(30) NOT NULL,
    unit VARCHAR(100),
    reference_range VARCHAR(255),
    qualitative_options VARCHAR(1000),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL,
    CONSTRAINT pk_analysis_definition_parameters PRIMARY KEY (id),
    CONSTRAINT fk_analysis_definition_parameters_definition FOREIGN KEY (analysis_definition_id)
        REFERENCES analysis_definitions (id),
    CONSTRAINT uk_analysis_definition_parameters_code UNIQUE (analysis_definition_id, code)
);

ALTER TABLE analysis_requests
    ADD COLUMN analysis_definition_id UUID,
    ADD COLUMN requested_specimen_type VARCHAR(30),
    ADD CONSTRAINT fk_analysis_requests_definition FOREIGN KEY (analysis_definition_id)
        REFERENCES analysis_definitions (id);

CREATE TABLE analysis_request_parameters (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    analysis_request_id UUID NOT NULL,
    source_parameter_id UUID,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    value_type VARCHAR(30) NOT NULL,
    unit VARCHAR(100),
    reference_range VARCHAR(255),
    qualitative_options VARCHAR(1000),
    required BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT pk_analysis_request_parameters PRIMARY KEY (id),
    CONSTRAINT fk_analysis_request_parameters_request FOREIGN KEY (analysis_request_id)
        REFERENCES analysis_requests (id),
    CONSTRAINT fk_analysis_request_parameters_source FOREIGN KEY (source_parameter_id)
        REFERENCES analysis_definition_parameters (id),
    CONSTRAINT uk_analysis_request_parameters_code UNIQUE (analysis_request_id, code)
);

CREATE TABLE analysis_result_values (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    analysis_result_id UUID NOT NULL,
    request_parameter_id UUID NOT NULL,
    numeric_value NUMERIC(19, 6),
    integer_value BIGINT,
    text_value VARCHAR(2000),
    boolean_value BOOLEAN,
    qualitative_value VARCHAR(255),
    abnormal_flag VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    comment VARCHAR(1000),
    CONSTRAINT pk_analysis_result_values PRIMARY KEY (id),
    CONSTRAINT fk_analysis_result_values_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id),
    CONSTRAINT fk_analysis_result_values_parameter FOREIGN KEY (request_parameter_id)
        REFERENCES analysis_request_parameters (id),
    CONSTRAINT uk_analysis_result_values_parameter UNIQUE (analysis_result_id, request_parameter_id)
);

CREATE TABLE disease_definitions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    icd_system VARCHAR(30),
    icd_code VARCHAR(50),
    icd_uri VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(100),
    CONSTRAINT pk_disease_definitions PRIMARY KEY (id),
    CONSTRAINT uk_disease_definitions_code UNIQUE (code)
);

CREATE UNIQUE INDEX uk_disease_definitions_icd
    ON disease_definitions (icd_system, icd_code)
    WHERE icd_system IS NOT NULL AND icd_code IS NOT NULL;

CREATE TABLE analysis_result_interpretations (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    analysis_result_id UUID NOT NULL,
    clinical_conclusion VARCHAR(2000) NOT NULL,
    doctor_username VARCHAR(100) NOT NULL,
    interpreted_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_analysis_result_interpretations PRIMARY KEY (id),
    CONSTRAINT fk_analysis_result_interpretations_result FOREIGN KEY (analysis_result_id)
        REFERENCES analysis_results (id),
    CONSTRAINT uk_analysis_result_interpretations_result UNIQUE (analysis_result_id)
);

CREATE TABLE analysis_result_interpretation_diseases (
    interpretation_id UUID NOT NULL,
    disease_id UUID NOT NULL,
    CONSTRAINT pk_analysis_result_interpretation_diseases PRIMARY KEY (interpretation_id, disease_id),
    CONSTRAINT fk_analysis_result_interpretation_diseases_interpretation FOREIGN KEY (interpretation_id)
        REFERENCES analysis_result_interpretations (id),
    CONSTRAINT fk_analysis_result_interpretation_diseases_disease FOREIGN KEY (disease_id)
        REFERENCES disease_definitions (id)
);

CREATE INDEX idx_analysis_definitions_active_name ON analysis_definitions (active, name);
CREATE INDEX idx_disease_definitions_active_name ON disease_definitions (active, name);
CREATE INDEX idx_analysis_request_parameters_request_order
    ON analysis_request_parameters (analysis_request_id, display_order);

-- Profil minimal livré avec la plateforme. Les intervalles biologiques restent
-- volontairement descriptifs : ils doivent être configurés par le laboratoire
-- selon sa méthode, l'âge et le sexe du patient.
WITH definition AS (
    INSERT INTO analysis_definitions (
        id, code, name, description, specimen_type, active, created_at, created_by
    ) VALUES (
        gen_random_uuid(),
        'HEMOGRAM',
        'Hémogramme (NFS)',
        'Numération formule sanguine réalisée sur sang total.',
        'BLOOD',
        TRUE,
        CURRENT_TIMESTAMP,
        'migration'
    )
    RETURNING id
)
INSERT INTO analysis_definition_parameters (
    id, analysis_definition_id, code, name, value_type, unit,
    reference_range, qualitative_options, required, display_order
)
SELECT gen_random_uuid(), id, 'HEMOGLOBIN', 'Hémoglobine', 'DECIMAL', 'g/dL',
       'Selon la méthode du laboratoire, l’âge et le sexe', NULL, TRUE, 1 FROM definition
UNION ALL
SELECT gen_random_uuid(), id, 'HEMATOCRIT', 'Hématocrite', 'PERCENTAGE', '%',
       'Selon la méthode du laboratoire, l’âge et le sexe', NULL, TRUE, 2 FROM definition
UNION ALL
SELECT gen_random_uuid(), id, 'LEUKOCYTES', 'Leucocytes', 'DECIMAL', 'G/L',
       'Selon la méthode du laboratoire et l’âge', NULL, TRUE, 3 FROM definition
UNION ALL
SELECT gen_random_uuid(), id, 'PLATELETS', 'Plaquettes', 'INTEGER', 'G/L',
       'Selon la méthode du laboratoire et l’âge', NULL, TRUE, 4 FROM definition;

INSERT INTO disease_definitions (
    id, code, name, description, active, created_at, created_by
) VALUES
    (gen_random_uuid(), 'MAL-MALARIA', 'Paludisme', 'Diagnostic clinique à confirmer selon les examens et protocoles applicables.', TRUE, CURRENT_TIMESTAMP, 'migration'),
    (gen_random_uuid(), 'MAL-ANEMIA', 'Anémie', 'Diagnostic clinique à préciser selon le contexte et les résultats biologiques.', TRUE, CURRENT_TIMESTAMP, 'migration'),
    (gen_random_uuid(), 'MAL-UTI', 'Infection urinaire', 'Diagnostic clinique à préciser selon le contexte et les résultats biologiques.', TRUE, CURRENT_TIMESTAMP, 'migration'),
    (gen_random_uuid(), 'MAL-TYPHOID', 'Fièvre typhoïde', 'Diagnostic clinique à confirmer selon les examens et protocoles applicables.', TRUE, CURRENT_TIMESTAMP, 'migration');
