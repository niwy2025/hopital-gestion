-- Une délivrance est une opération de caisse avant l'arrivée du module Comptabilité.
-- Les colonnes restent nulles pour l'historique déjà existant : il ne faut jamais
-- inventer un paiement à zéro. Les nouvelles délivrances sont validées par l'API.
ALTER TABLE patient_passage_prescription_dispenses
    ADD COLUMN paid_amount NUMERIC(14, 2),
    ADD COLUMN payment_currency VARCHAR(3),
    ADD COLUMN payment_method VARCHAR(30),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_paid_amount
        CHECK (paid_amount >= 0),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_payment_currency
        CHECK (payment_currency IN ('CDF', 'USD')),
    ADD CONSTRAINT ck_patient_passage_prescription_dispenses_payment_method
        CHECK (payment_method IN ('CASH', 'MOBILE_MONEY', 'BANK_CARD', 'BANK_TRANSFER', 'INSURANCE', 'OTHER'));
