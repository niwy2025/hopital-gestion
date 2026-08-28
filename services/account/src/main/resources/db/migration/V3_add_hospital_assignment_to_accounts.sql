ALTER TABLE accounts ADD COLUMN hospital_id UUID NULL;

CREATE INDEX ix_accounts_hospital_id ON accounts (hospital_id);
