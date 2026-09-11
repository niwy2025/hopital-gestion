-- Registre administratif de facturation : pagination par établissement et date.
CREATE INDEX idx_patient_passages_billing_search
    ON patient_passages(hospital_id, arrived_at DESC, id DESC);
