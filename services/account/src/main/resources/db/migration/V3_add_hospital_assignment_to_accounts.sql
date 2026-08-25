ALTER TABLE accounts ADD hospital_id UNIQUEIDENTIFIER NULL;

CREATE INDEX IX_accounts_hospital_id ON accounts (hospital_id);
