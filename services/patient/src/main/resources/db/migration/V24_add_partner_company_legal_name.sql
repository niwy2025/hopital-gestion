ALTER TABLE partner_companies
    ADD COLUMN legal_name VARCHAR(200);

CREATE INDEX idx_partner_companies_hospital_legal_name
    ON partner_companies (hospital_id, legal_name);
