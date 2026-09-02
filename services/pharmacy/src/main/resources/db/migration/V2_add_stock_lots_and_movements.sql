CREATE TABLE stock_lots (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    stock_id UUID NOT NULL REFERENCES hospital_medicine_stocks(id),
    stock_entry_id UUID NOT NULL UNIQUE REFERENCES stock_entries(id),
    received_quantity INTEGER NOT NULL CHECK (received_quantity > 0),
    remaining_quantity INTEGER NOT NULL CHECK (remaining_quantity >= 0),
    unit_cost NUMERIC(18, 2) NOT NULL CHECK (unit_cost > 0),
    currency VARCHAR(3) NOT NULL,
    expires_on DATE,
    received_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    stock_id UUID NOT NULL REFERENCES hospital_medicine_stocks(id),
    stock_lot_id UUID REFERENCES stock_lots(id),
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    medicine_id UUID NOT NULL REFERENCES medicines(id),
    type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(18, 2) NOT NULL CHECK (unit_cost >= 0),
    currency VARCHAR(3) NOT NULL,
    notes VARCHAR(2000),
    occurred_at TIMESTAMPTZ NOT NULL,
    performed_by_user_id VARCHAR(100) NOT NULL,
    performed_by_username VARCHAR(150) NOT NULL
);

-- Les entrées déjà présentes deviennent des lots encore entièrement disponibles :
-- aucune sortie n'existait avant l'introduction du grand livre de stock.
INSERT INTO stock_lots (
    id, code, stock_id, stock_entry_id, received_quantity, remaining_quantity,
    unit_cost, currency, expires_on, received_at
)
SELECT gen_random_uuid(), CONCAT('LOT-', entry.code), entry.stock_id, entry.id,
       entry.quantity, entry.quantity, entry.unit_cost, entry.currency, entry.expires_on, entry.received_at
FROM stock_entries AS entry;

INSERT INTO stock_movements (
    id, code, stock_id, stock_lot_id, hospital_id, hospital_code, medicine_id,
    type, quantity, unit_cost, currency, notes, occurred_at, performed_by_user_id, performed_by_username
)
SELECT gen_random_uuid(), CONCAT('MVT-', entry.code), entry.stock_id, lot.id, entry.hospital_id, entry.hospital_code,
       entry.medicine_id, 'ENTRY', entry.quantity, entry.unit_cost, entry.currency,
       entry.notes, entry.received_at, entry.received_by_user_id, entry.received_by_username
FROM stock_entries AS entry
JOIN stock_lots AS lot ON lot.stock_entry_id = entry.id;

CREATE INDEX idx_stock_lots_stock_expiry ON stock_lots (stock_id, expires_on, received_at);
CREATE INDEX idx_stock_movements_scope ON stock_movements (hospital_code, occurred_at DESC);
CREATE INDEX idx_stock_movements_medicine ON stock_movements (medicine_id, occurred_at DESC);
