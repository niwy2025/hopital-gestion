CREATE TABLE analysis_requests (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    reference_laboratory_code NVARCHAR(30) NOT NULL,
    patient_reference NVARCHAR(100) NOT NULL,
    patient_name NVARCHAR(200) NOT NULL,
    analysis_code NVARCHAR(50) NOT NULL,
    analysis_name NVARCHAR(200) NOT NULL,
    requester_name NVARCHAR(200) NULL,
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL,
    CONSTRAINT PK_analysis_requests PRIMARY KEY (id),
    CONSTRAINT UK_analysis_requests_code UNIQUE (code)
);

CREATE TABLE specimens (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    analysis_request_id UNIQUEIDENTIFIER NOT NULL,
    specimen_type NVARCHAR(30) NOT NULL,
    status NVARCHAR(30) NOT NULL,
    collected_at DATETIME2 NOT NULL,
    received_at DATETIME2 NOT NULL,
    CONSTRAINT PK_specimens PRIMARY KEY (id),
    CONSTRAINT UK_specimens_code UNIQUE (code),
    CONSTRAINT FK_specimens_analysis_request FOREIGN KEY (analysis_request_id) REFERENCES analysis_requests (id)
);

CREATE TABLE analysis_results (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    analysis_request_id UNIQUEIDENTIFIER NOT NULL,
    result_value NVARCHAR(1000) NOT NULL,
    unit NVARCHAR(100) NULL,
    reference_range NVARCHAR(255) NULL,
    comment NVARCHAR(1000) NULL,
    status NVARCHAR(30) NOT NULL,
    entered_at DATETIME2 NOT NULL,
    validated_at DATETIME2 NULL,
    validated_by NVARCHAR(100) NULL,
    CONSTRAINT PK_analysis_results PRIMARY KEY (id),
    CONSTRAINT UK_analysis_results_code UNIQUE (code),
    CONSTRAINT UK_analysis_results_analysis_request UNIQUE (analysis_request_id),
    CONSTRAINT FK_analysis_results_analysis_request FOREIGN KEY (analysis_request_id) REFERENCES analysis_requests (id)
);
