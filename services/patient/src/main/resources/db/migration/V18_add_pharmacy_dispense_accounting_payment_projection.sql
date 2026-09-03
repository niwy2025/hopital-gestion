-- Accounting owns the invoice balance. A pharmacy dispense keeps a versioned
-- read projection so patient/pharmacy screens do not present the initial
-- amount collected as the current balance after a later cashier settlement.
ALTER TABLE patient_passage_prescription_dispenses
    ADD COLUMN accounting_sync_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUESTED',
    ADD COLUMN accounting_invoice_id UUID,
    ADD COLUMN accounting_invoice_code VARCHAR(80),
    ADD COLUMN accounting_total_amount NUMERIC(14, 2),
    ADD COLUMN accounting_paid_amount NUMERIC(14, 2),
    ADD COLUMN accounting_due_amount NUMERIC(14, 2),
    ADD COLUMN accounting_currency VARCHAR(3),
    ADD COLUMN accounting_invoice_status VARCHAR(30),
    ADD COLUMN accounting_state_version BIGINT,
    ADD COLUMN accounting_last_payment_id UUID,
    ADD COLUMN accounting_last_paid_on DATE,
    ADD COLUMN accounting_last_payment_reference VARCHAR(160),
    ADD COLUMN accounting_synchronized_at TIMESTAMP(6) WITH TIME ZONE,
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_sync_status
        CHECK (accounting_sync_status IN ('NOT_REQUESTED', 'PENDING', 'SYNCHRONIZED')),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_currency
        CHECK (accounting_currency IS NULL OR accounting_currency IN ('CDF', 'USD')),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_invoice_status
        CHECK (accounting_invoice_status IS NULL OR accounting_invoice_status IN (
            'DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_amounts
        CHECK (
            accounting_total_amount IS NULL OR (
                accounting_total_amount >= 0
                AND accounting_paid_amount >= 0
                AND accounting_due_amount >= 0
                AND accounting_paid_amount <= accounting_total_amount
                AND accounting_due_amount <= accounting_total_amount
                AND accounting_paid_amount + accounting_due_amount = accounting_total_amount
            )
        ),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_projection_complete
        CHECK (
            accounting_sync_status <> 'SYNCHRONIZED' OR (
                accounting_invoice_id IS NOT NULL
                AND accounting_invoice_code IS NOT NULL
                AND accounting_total_amount IS NOT NULL
                AND accounting_paid_amount IS NOT NULL
                AND accounting_due_amount IS NOT NULL
                AND accounting_currency IS NOT NULL
                AND accounting_invoice_status IS NOT NULL
                AND accounting_state_version IS NOT NULL
                AND accounting_synchronized_at IS NOT NULL
            )
        ),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_accounting_state_version
        CHECK (accounting_state_version IS NULL OR accounting_state_version >= 0);

-- Existing outbox rows are already waiting for accounting and should not be
-- presented as if no accounting action had been requested.
UPDATE patient_passage_prescription_dispenses dispense
SET accounting_sync_status = 'PENDING'
WHERE EXISTS (
    SELECT 1
    FROM patient_pharmacy_accounting_outbox_events outbox
    WHERE outbox.dispense_id = dispense.id
      AND outbox.status = 'PENDING'
);

CREATE INDEX idx_patient_passage_prescription_dispenses_accounting_invoice
    ON patient_passage_prescription_dispenses (accounting_invoice_id);

-- Received events are retained for an audit trail and are the idempotency
-- boundary for asynchronous accounting retries.
CREATE TABLE patient_pharmacy_dispense_payment_settlement_events (
    event_id UUID NOT NULL,
    dispense_id UUID NOT NULL,
    payment_id UUID,
    invoice_id UUID NOT NULL,
    invoice_code VARCHAR(80) NOT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    paid_amount NUMERIC(14, 2) NOT NULL,
    due_amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    invoice_status VARCHAR(30) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    state_version BIGINT NOT NULL,
    paid_on DATE,
    payment_reference VARCHAR(160),
    applied BOOLEAN NOT NULL,
    ignored_reason VARCHAR(250),
    received_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_patient_pharmacy_dispense_payment_settlement_events PRIMARY KEY (event_id),
    CONSTRAINT uk_patient_pharmacy_dispense_payment_settlement_events_payment_id UNIQUE (payment_id),
    CONSTRAINT fk_patient_pharmacy_dispense_payment_settlement_events_dispense
        FOREIGN KEY (dispense_id) REFERENCES patient_passage_prescription_dispenses (id) ON DELETE RESTRICT,
    CONSTRAINT ck_patient_pharmacy_dispense_payment_settlement_events_amounts
        CHECK (
            total_amount >= 0
            AND paid_amount >= 0
            AND due_amount >= 0
            AND paid_amount <= total_amount
            AND due_amount <= total_amount
            AND paid_amount + due_amount = total_amount
        ),
    CONSTRAINT ck_patient_pharmacy_dispense_payment_settlement_events_currency
        CHECK (currency IN ('CDF', 'USD')),
    CONSTRAINT ck_patient_pharmacy_dispense_payment_settlement_events_status
        CHECK (invoice_status IN ('DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    CONSTRAINT ck_patient_pharmacy_dispense_payment_settlement_events_event_type
        CHECK (event_type IN ('INVOICE_ISSUED', 'PAYMENT_RECORDED')),
    CONSTRAINT ck_patient_pharmacy_dispense_payment_settlement_events_state_version
        CHECK (state_version >= 0)
);

CREATE INDEX idx_patient_pharmacy_dispense_payment_settlement_events_dispense
    ON patient_pharmacy_dispense_payment_settlement_events (dispense_id, state_version DESC);
