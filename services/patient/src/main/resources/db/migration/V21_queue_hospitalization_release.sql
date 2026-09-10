-- Une sortie validée reste livrable même après un arrêt d'un des services.
CREATE TABLE patient_hospitalization_outbox (
    id UUID PRIMARY KEY,
    passage_id UUID NOT NULL UNIQUE REFERENCES patient_passages(id),
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','DELIVERED')),
    attempts INTEGER NOT NULL DEFAULT 0,
    retry_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    delivered_at TIMESTAMPTZ
);
CREATE INDEX idx_patient_hospitalization_pending ON patient_hospitalization_outbox(retry_at) WHERE status='PENDING';
