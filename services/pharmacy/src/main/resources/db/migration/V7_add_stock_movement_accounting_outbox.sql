-- Une sortie de stock reste validée lorsque le service Comptabilité est
-- indisponible. La clé unique rend la reprise strictement idempotente.
CREATE TABLE pharmacy_stock_movement_accounting_outbox_events (
    id UUID PRIMARY KEY,
    stock_movement_id UUID NOT NULL UNIQUE REFERENCES stock_movements(id),
    stock_movement_code VARCHAR(30) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,
    accounting_entry_reference VARCHAR(80),
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pharmacy_stock_movement_accounting_outbox_pending
    ON pharmacy_stock_movement_accounting_outbox_events (status, next_attempt_at, created_at);

-- Les délivrances patient DSP sont déjà rapprochées depuis l'outbox patient :
-- elles ne doivent jamais donner lieu à une seconde écriture de sortie ici.
INSERT INTO pharmacy_stock_movement_accounting_outbox_events (
    id, stock_movement_id, stock_movement_code, status, attempt_count,
    next_attempt_at, created_at, updated_at
)
SELECT gen_random_uuid(), movement.id, movement.code, 'PENDING', 0, NOW(), NOW(), NOW()
FROM stock_movements movement
WHERE movement.type IN ('LOSS', 'EXPIRY', 'TRANSFER_OUT', 'DISPENSING')
  AND UPPER(COALESCE(movement.source_type, '')) <> 'PRESCRIPTION_DISPENSE'
  AND NOT (
      movement.type = 'DISPENSING'
      AND UPPER(COALESCE(movement.source_code, '')) LIKE 'DSP-%'
  )
ON CONFLICT (stock_movement_id) DO NOTHING;
