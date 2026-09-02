-- Le patient-service conserve la référence UUID du catalogue sans clé étrangère
-- inter-service. Elle permet à la pharmacie d'identifier le produit à délivrer.
ALTER TABLE patient_passage_prescription_items
    ADD COLUMN medicine_id UUID;

CREATE INDEX idx_patient_passage_prescription_items_medicine
    ON patient_passage_prescription_items (medicine_id)
    WHERE medicine_id IS NOT NULL;
