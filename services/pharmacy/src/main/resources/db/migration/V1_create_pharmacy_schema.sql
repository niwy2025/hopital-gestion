CREATE TABLE medicines (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    generic_name VARCHAR(200) NOT NULL,
    commercial_name VARCHAR(200),
    dosage VARCHAR(100),
    pharmaceutical_form VARCHAR(100),
    presentation VARCHAR(150),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL
);

CREATE TABLE hospital_medicine_stocks (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    medicine_id UUID NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    reorder_level INTEGER NOT NULL DEFAULT 0 CHECK (reorder_level >= 0),
    average_unit_cost NUMERIC(18, 2) NOT NULL CHECK (average_unit_cost >= 0),
    currency VARCHAR(3) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_hospital_medicine_stock UNIQUE (hospital_id, medicine_id)
);

CREATE TABLE stock_entries (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    stock_id UUID NOT NULL REFERENCES hospital_medicine_stocks(id),
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    medicine_id UUID NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(18, 2) NOT NULL CHECK (unit_cost > 0),
    total_cost NUMERIC(18, 2) NOT NULL CHECK (total_cost > 0),
    currency VARCHAR(3) NOT NULL,
    expires_on DATE,
    supplier_name VARCHAR(200),
    notes VARCHAR(2000),
    accounting_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACCOUNTING',
    received_at TIMESTAMPTZ NOT NULL,
    received_by_user_id VARCHAR(100) NOT NULL,
    received_by_username VARCHAR(150) NOT NULL
);

CREATE INDEX idx_medicines_search ON medicines (generic_name);
CREATE INDEX idx_hospital_medicine_stocks_scope ON hospital_medicine_stocks (hospital_code, updated_at DESC);
CREATE INDEX idx_stock_entries_scope ON stock_entries (hospital_code, received_at DESC);
CREATE INDEX idx_stock_entries_accounting ON stock_entries (accounting_status, received_at DESC);
