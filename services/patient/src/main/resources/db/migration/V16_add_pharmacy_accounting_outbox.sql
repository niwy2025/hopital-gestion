-- A pharmacy dispense is durable even when the accounting service is temporarily unavailable.
-- The outbox makes the synchronization retryable and prevents a second document for one dispense.
CREATE TABLE patient_pharmacy_accounting_outbox_events (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    dispense_id UUID NOT NULL,
    dispense_code VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP(6) WITH TIME ZONE,
    invoice_reference VARCHAR(80),
    last_error TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_patient_pharmacy_accounting_outbox_events PRIMARY KEY (id),
    CONSTRAINT uk_patient_pharmacy_accounting_outbox_events_dispense_id UNIQUE (dispense_id),
    CONSTRAINT uk_patient_pharmacy_accounting_outbox_events_dispense_code UNIQUE (dispense_code),
    CONSTRAINT fk_patient_pharmacy_accounting_outbox_events_dispense
        FOREIGN KEY (dispense_id) REFERENCES patient_passage_prescription_dispenses (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_pharmacy_accounting_outbox_events_status
        CHECK (status IN ('PENDING', 'POSTED')),
    CONSTRAINT ck_patient_pharmacy_accounting_outbox_events_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_patient_pharmacy_accounting_outbox_events_retry
    ON patient_pharmacy_accounting_outbox_events (status, next_attempt_at, created_at);
