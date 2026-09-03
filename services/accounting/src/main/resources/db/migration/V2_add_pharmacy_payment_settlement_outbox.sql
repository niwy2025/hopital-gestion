-- Les règlements comptables d'une délivrance pharmacie sont projetés vers le
-- dossier patient de manière asynchrone. L'état envoyé est un instantané
-- versionné : un retry tardif ne doit jamais rétablir un ancien solde.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE accounting_invoices
    ADD COLUMN settlement_version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE accounting_payments
    ADD COLUMN idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX uk_accounting_payments_invoice_idempotency_key
    ON accounting_payments (invoice_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE TABLE accounting_pharmacy_payment_settlement_outbox_events (
    id UUID NOT NULL,
    event_key VARCHAR(120) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    payment_id UUID,
    invoice_id UUID NOT NULL,
    dispense_code VARCHAR(80) NOT NULL,
    invoice_code VARCHAR(50) NOT NULL,
    total_amount NUMERIC(18,2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(18,2) NOT NULL CHECK (paid_amount >= 0),
    due_amount NUMERIC(18,2) NOT NULL CHECK (due_amount >= 0),
    currency VARCHAR(3) NOT NULL,
    invoice_status VARCHAR(20) NOT NULL,
    state_version INTEGER NOT NULL CHECK (state_version >= 0),
    paid_on DATE,
    payment_reference VARCHAR(150),
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    next_attempt_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_accounting_pharmacy_payment_settlement_outbox_events PRIMARY KEY (id),
    CONSTRAINT uk_accounting_pharmacy_payment_settlement_outbox_event_key UNIQUE (event_key),
    CONSTRAINT uk_accounting_pharmacy_payment_settlement_outbox_payment_id UNIQUE (payment_id),
    CONSTRAINT fk_accounting_pharmacy_payment_settlement_outbox_invoice
        FOREIGN KEY (invoice_id) REFERENCES accounting_invoices(id) ON DELETE RESTRICT,
    CONSTRAINT fk_accounting_pharmacy_payment_settlement_outbox_payment
        FOREIGN KEY (payment_id) REFERENCES accounting_payments(id) ON DELETE RESTRICT,
    CONSTRAINT ck_accounting_pharmacy_payment_settlement_outbox_event_type
        CHECK (event_type IN ('INVOICE_ISSUED', 'PAYMENT_RECORDED')),
    CONSTRAINT ck_accounting_pharmacy_payment_settlement_outbox_status
        CHECK (status IN ('PENDING', 'POSTED')),
    CONSTRAINT ck_accounting_pharmacy_payment_settlement_outbox_amounts
        CHECK (total_amount = paid_amount + due_amount)
);

CREATE INDEX idx_accounting_pharmacy_payment_settlement_outbox_retry
    ON accounting_pharmacy_payment_settlement_outbox_events (status, next_attempt_at, created_at);

-- Une facture pharmacie préexistante doit aussi pouvoir être rapprochée par le
-- nouveau consommateur, même si aucun nouveau paiement ne lui est appliqué.
INSERT INTO accounting_pharmacy_payment_settlement_outbox_events (
    id, event_key, event_type, payment_id, invoice_id, dispense_code, invoice_code,
    total_amount, paid_amount, due_amount, currency, invoice_status, state_version,
    paid_on, payment_reference, status, attempt_count, next_attempt_at,
    processed_at, last_error, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    'INVOICE:' || invoice.id::TEXT || ':' || invoice.settlement_version::TEXT,
    'INVOICE_ISSUED',
    NULL,
    invoice.id,
    invoice.source_code,
    invoice.code,
    invoice.total_amount,
    invoice.paid_amount,
    invoice.due_amount,
    invoice.currency,
    invoice.status,
    invoice.settlement_version,
    NULL,
    NULL,
    'PENDING',
    0,
    NOW(),
    NULL,
    NULL,
    NOW(),
    NOW()
FROM accounting_invoices invoice
WHERE invoice.source_type = 'PHARMACY_DISPENSE';
