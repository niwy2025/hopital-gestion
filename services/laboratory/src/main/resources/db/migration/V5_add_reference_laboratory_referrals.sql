-- A reference-laboratory referral starts at the hospital, then moves through
-- collection, transport and physical reception. Existing internal requests
-- keep their historical values and remain compatible with the old workflow.
ALTER TABLE analysis_requests
    ADD COLUMN origin_hospital_id UUID,
    ADD COLUMN origin_hospital_code VARCHAR(30),
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',
    ADD COLUMN clinical_indication VARCHAR(1000);

ALTER TABLE specimens
    ALTER COLUMN received_at DROP NOT NULL,
    ADD COLUMN collected_by VARCHAR(100),
    ADD COLUMN collection_note VARCHAR(1000),
    ADD COLUMN dispatched_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN dispatched_by VARCHAR(100),
    ADD COLUMN carrier_name VARCHAR(200),
    ADD COLUMN dispatch_note VARCHAR(1000),
    ADD COLUMN received_by VARCHAR(100),
    ADD COLUMN reception_condition VARCHAR(1000),
    ADD COLUMN rejection_reason VARCHAR(1000);

CREATE TABLE analysis_request_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    analysis_request_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    actor_username VARCHAR(100),
    note VARCHAR(1000),
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_analysis_request_events PRIMARY KEY (id),
    CONSTRAINT fk_analysis_request_events_request FOREIGN KEY (analysis_request_id)
        REFERENCES analysis_requests (id)
);

CREATE INDEX idx_analysis_request_events_request_occurred_at
    ON analysis_request_events (analysis_request_id, occurred_at ASC);

CREATE INDEX idx_analysis_requests_origin_hospital_created_at
    ON analysis_requests (origin_hospital_id, created_at DESC);
