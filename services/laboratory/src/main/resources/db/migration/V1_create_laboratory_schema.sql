CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE analysis_requests (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    reference_laboratory_code VARCHAR(30) NOT NULL,
    patient_reference VARCHAR(100) NOT NULL,
    patient_name VARCHAR(200) NOT NULL,
    analysis_code VARCHAR(50) NOT NULL,
    analysis_name VARCHAR(200) NOT NULL,
    requester_name VARCHAR(200),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT PK_analysis_requests PRIMARY KEY (id),
    CONSTRAINT UK_analysis_requests_code UNIQUE (code)
);

CREATE TABLE specimens (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    analysis_request_id UUID NOT NULL,
    specimen_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    collected_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    received_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT PK_specimens PRIMARY KEY (id),
    CONSTRAINT UK_specimens_code UNIQUE (code),
    CONSTRAINT FK_specimens_analysis_request FOREIGN KEY (analysis_request_id) REFERENCES analysis_requests (id)
);

CREATE TABLE analysis_results (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    analysis_request_id UUID NOT NULL,
    result_value VARCHAR(1000) NOT NULL,
    unit VARCHAR(100),
    reference_range VARCHAR(255),
    comment VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    entered_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    validated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    validated_by VARCHAR(100),
    CONSTRAINT PK_analysis_results PRIMARY KEY (id),
    CONSTRAINT UK_analysis_results_code UNIQUE (code),
    CONSTRAINT UK_analysis_results_analysis_request UNIQUE (analysis_request_id),
    CONSTRAINT FK_analysis_results_analysis_request FOREIGN KEY (analysis_request_id) REFERENCES analysis_requests (id)
);
