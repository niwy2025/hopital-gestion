-- Lot 2 : les frais de passage peuvent être regroupés en facture émise (+ écriture VEN).
ALTER TABLE billing_passage_charges
    DROP CONSTRAINT IF EXISTS billing_passage_charges_status_check;

ALTER TABLE billing_passage_charges
    ADD CONSTRAINT billing_passage_charges_status_check
    CHECK (status IN ('UNBILLED', 'BILLED', 'CANCELLED'));

ALTER TABLE billing_passage_charges
    ADD COLUMN IF NOT EXISTS invoice_id UUID,
    ADD COLUMN IF NOT EXISTS invoice_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS billed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS billed_by_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS billed_by VARCHAR(150);

CREATE INDEX IF NOT EXISTS idx_billing_charges_invoice
    ON billing_passage_charges (invoice_id)
    WHERE invoice_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_billing_charges_passage_unbilled
    ON billing_passage_charges (hospital_id, passage_id, status, currency)
    WHERE status = 'UNBILLED';
