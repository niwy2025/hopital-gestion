-- La réception reste valide même si la comptabilité est momentanément
-- indisponible. Une seule réception crée une seule intention comptable.
ALTER TABLE stock_entries
    ADD COLUMN accounting_entry_reference VARCHAR(80);

CREATE TABLE pharmacy_stock_entry_accounting_outbox_events (
    id UUID PRIMARY KEY,
    stock_entry_id UUID NOT NULL UNIQUE REFERENCES stock_entries(id),
    stock_entry_code VARCHAR(30) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,
    accounting_entry_reference VARCHAR(80),
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pharmacy_stock_entry_accounting_outbox_pending
    ON pharmacy_stock_entry_accounting_outbox_events (status, next_attempt_at, created_at);

-- Les réceptions historiques encore non comptabilisées sont reprises une fois
-- par la même file. La clé unique protège aussi le redémarrage/migration.
INSERT INTO pharmacy_stock_entry_accounting_outbox_events (
    id, stock_entry_id, stock_entry_code, status, attempt_count, next_attempt_at,
    created_at, updated_at
)
SELECT gen_random_uuid(), entry.id, entry.code, 'PENDING', 0, NOW(), NOW(), NOW()
FROM stock_entries entry
WHERE entry.accounting_status = 'PENDING_ACCOUNTING'
ON CONFLICT (stock_entry_id) DO NOTHING;
