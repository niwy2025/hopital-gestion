CREATE TABLE accounting_fixed_assets (
    id UUID PRIMARY KEY, hospital_id UUID NOT NULL, asset_code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL, source_cost NUMERIC(19,2), source_currency VARCHAR(3) NOT NULL, currency VARCHAR(3) NOT NULL,
    received_on DATE NOT NULL, asset_status VARCHAR(30) NOT NULL, source_version INTEGER NOT NULL,
    classification VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK(classification IN ('PENDING','EXPENSE','IMMOBILIZATION','THIRD_PARTY')),
    accounting_value NUMERIC(19,2) CHECK(accounting_value >= 0), residual_value NUMERIC(19,2) CHECK(residual_value >= 0),
    start_on DATE, useful_life_months INTEGER CHECK(useful_life_months BETWEEN 1 AND 1200),
    asset_account_id UUID REFERENCES accounting_accounts(id), depreciation_account_id UUID REFERENCES accounting_accounts(id),
    expense_account_id UUID REFERENCES accounting_accounts(id), depreciable BOOLEAN NOT NULL DEFAULT FALSE,
    review_note VARCHAR(4000), reviewed_by VARCHAR(150), reviewed_at TIMESTAMPTZ,
    source_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_accounting_fixed_assets_hospital ON accounting_fixed_assets(hospital_id,classification,name);
CREATE TABLE accounting_fixed_asset_installments (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL REFERENCES accounting_fixed_assets(id),
    installment INTEGER NOT NULL, entry_id UUID NOT NULL REFERENCES accounting_entries(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(asset_id,installment)
);
CREATE TABLE accounting_fixed_asset_reviews (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL REFERENCES accounting_fixed_assets(id),
    classification VARCHAR(30) NOT NULL, accounting_value NUMERIC(19,2), residual_value NUMERIC(19,2),
    start_on DATE, useful_life_months INTEGER, asset_account_id UUID, depreciation_account_id UUID, expense_account_id UUID,
    depreciable BOOLEAN NOT NULL, note VARCHAR(4000) NOT NULL, operator_id VARCHAR(100) NOT NULL,
    operator_name VARCHAR(150) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
