-- `paid_amount` records the cash/transfer received. `total_amount` is the
-- server-calculated invoice value from pharmacy stock selling prices.
ALTER TABLE patient_passage_prescription_dispenses
    ADD COLUMN total_amount NUMERIC(14, 2),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_total_amount
        CHECK (total_amount IS NULL OR total_amount >= 0),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_paid_not_over_total
        CHECK (total_amount IS NULL OR paid_amount IS NULL OR paid_amount <= total_amount);
