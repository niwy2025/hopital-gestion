ALTER TABLE analysis_requests
    ADD COLUMN laboratory_type VARCHAR(30) NOT NULL DEFAULT 'REFERENCE';

ALTER TABLE analysis_requests
    RENAME COLUMN reference_laboratory_code TO laboratory_code;
