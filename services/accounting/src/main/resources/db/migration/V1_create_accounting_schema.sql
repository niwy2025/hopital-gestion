-- Socle comptable hospitalier. Le plan est initialisé par hôpital depuis un
-- sous-ensemble SYSCOHADA configurable ; les écritures postées restent
-- append-only et sont corrigées par contrepassation.

CREATE TABLE accounting_accounts (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    label VARCHAR(220) NOT NULL,
    account_class VARCHAR(2) NOT NULL,
    nature VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    system_account BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_accounting_accounts_hospital_number UNIQUE (hospital_id, account_number)
);

CREATE TABLE accounting_journals (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    code VARCHAR(20) NOT NULL,
    label VARCHAR(180) NOT NULL,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    system_journal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_accounting_journals_hospital_code UNIQUE (hospital_id, code)
);

CREATE TABLE accounting_periods (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    code VARCHAR(40) NOT NULL,
    label VARCHAR(180) NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    closed_at TIMESTAMPTZ,
    closed_by_user_id VARCHAR(100),
    closed_by_username VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_accounting_period_dates CHECK (ends_on >= starts_on),
    CONSTRAINT uk_accounting_periods_hospital_code UNIQUE (hospital_id, code)
);

CREATE TABLE accounting_entries (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    period_id UUID NOT NULL REFERENCES accounting_periods(id),
    journal_id UUID NOT NULL REFERENCES accounting_journals(id),
    journal_code VARCHAR(20) NOT NULL,
    code VARCHAR(50) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_code VARCHAR(80) NOT NULL,
    entry_date DATE NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_debit NUMERIC(18,2) NOT NULL CHECK (total_debit >= 0),
    total_credit NUMERIC(18,2) NOT NULL CHECK (total_credit >= 0),
    created_at TIMESTAMPTZ NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    posted_at TIMESTAMPTZ,
    posted_by_user_id VARCHAR(100),
    posted_by_username VARCHAR(150),
    reversal_entry_id UUID,
    CONSTRAINT uk_accounting_entries_hospital_code UNIQUE (hospital_id, code),
    CONSTRAINT uk_accounting_entries_source UNIQUE (hospital_id, source_type, source_code),
    CONSTRAINT ck_accounting_entries_balanced CHECK (total_debit = total_credit)
);

CREATE TABLE accounting_entry_lines (
    id UUID PRIMARY KEY,
    entry_id UUID NOT NULL REFERENCES accounting_entries(id),
    line_number INTEGER NOT NULL CHECK (line_number > 0),
    account_id UUID NOT NULL REFERENCES accounting_accounts(id),
    account_number VARCHAR(20) NOT NULL,
    account_label VARCHAR(220) NOT NULL,
    label VARCHAR(1000) NOT NULL,
    debit NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (debit >= 0),
    credit NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (credit >= 0),
    third_party_reference VARCHAR(100),
    CONSTRAINT uk_accounting_entry_lines_number UNIQUE (entry_id, line_number),
    CONSTRAINT ck_accounting_entry_line_one_side CHECK ((debit > 0 AND credit = 0) OR (credit > 0 AND debit = 0))
);

CREATE TABLE accounting_invoices (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_code VARCHAR(80) NOT NULL,
    patient_id UUID,
    patient_code VARCHAR(50),
    passage_id UUID,
    passage_code VARCHAR(50),
    issued_on DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_amount NUMERIC(18,2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    due_amount NUMERIC(18,2) NOT NULL CHECK (due_amount >= 0),
    description VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT uk_accounting_invoices_hospital_code UNIQUE (hospital_id, code),
    CONSTRAINT uk_accounting_invoices_source UNIQUE (hospital_id, source_type, source_code),
    CONSTRAINT ck_accounting_invoice_amounts CHECK (total_amount = paid_amount + due_amount)
);

CREATE TABLE accounting_payments (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    invoice_id UUID NOT NULL REFERENCES accounting_invoices(id),
    invoice_code VARCHAR(50) NOT NULL,
    paid_on DATE NOT NULL,
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    method VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(150),
    accounting_entry_id UUID NOT NULL REFERENCES accounting_entries(id),
    accounting_entry_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    received_by_user_id VARCHAR(100) NOT NULL,
    received_by_username VARCHAR(150) NOT NULL,
    CONSTRAINT uk_accounting_payments_hospital_code UNIQUE (hospital_id, code)
);

CREATE TABLE cash_sessions (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    opening_amount NUMERIC(18,2) NOT NULL CHECK (opening_amount >= 0),
    status VARCHAR(20) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    opened_by_user_id VARCHAR(100) NOT NULL,
    opened_by_username VARCHAR(150) NOT NULL,
    closed_at TIMESTAMPTZ,
    closed_by_user_id VARCHAR(100),
    closed_by_username VARCHAR(150),
    expected_closing_amount NUMERIC(18,2),
    declared_closing_amount NUMERIC(18,2),
    variance_amount NUMERIC(18,2),
    closing_notes VARCHAR(2000),
    CONSTRAINT uk_cash_sessions_hospital_code UNIQUE (hospital_id, code)
);

CREATE TABLE financial_statement_notes (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    period_id UUID REFERENCES accounting_periods(id),
    code VARCHAR(50) NOT NULL,
    title VARCHAR(240) NOT NULL,
    type VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_by_username VARCHAR(150) NOT NULL,
    validated_at TIMESTAMPTZ,
    validated_by_user_id VARCHAR(100),
    validated_by_username VARCHAR(150),
    CONSTRAINT uk_financial_statement_notes_hospital_code UNIQUE (hospital_id, code)
);

CREATE TABLE accounting_supporting_documents (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    hospital_code VARCHAR(30) NOT NULL,
    related_type VARCHAR(30) NOT NULL,
    related_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    content_base64 TEXT NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    uploaded_at TIMESTAMPTZ NOT NULL,
    uploaded_by_user_id VARCHAR(100) NOT NULL,
    uploaded_by_username VARCHAR(150) NOT NULL
);

CREATE INDEX idx_accounting_accounts_scope ON accounting_accounts (hospital_code, account_number);
CREATE INDEX idx_accounting_journals_scope ON accounting_journals (hospital_code, code);
CREATE INDEX idx_accounting_periods_scope ON accounting_periods (hospital_code, starts_on DESC);
CREATE INDEX idx_accounting_entries_scope ON accounting_entries (hospital_code, entry_date DESC);
CREATE INDEX idx_accounting_entries_period ON accounting_entries (period_id, status, entry_date DESC);
CREATE INDEX idx_accounting_entry_lines_account ON accounting_entry_lines (account_id, entry_id);
CREATE INDEX idx_accounting_invoices_scope ON accounting_invoices (hospital_code, issued_on DESC);
CREATE INDEX idx_accounting_payments_scope ON accounting_payments (hospital_code, paid_on DESC);
CREATE UNIQUE INDEX uk_cash_sessions_one_open_currency ON cash_sessions (hospital_id, currency) WHERE status = 'OPEN';
CREATE INDEX idx_cash_sessions_scope ON cash_sessions (hospital_code, opened_at DESC);
CREATE INDEX idx_financial_statement_notes_scope ON financial_statement_notes (hospital_code, period_id, status);
CREATE INDEX idx_accounting_supporting_documents_related ON accounting_supporting_documents (hospital_id, related_type, related_id);
