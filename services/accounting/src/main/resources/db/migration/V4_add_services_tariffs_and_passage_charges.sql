CREATE TABLE billing_services (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('CONSULTATION','LABORATORY','CARE','HOSPITALIZATION','OTHER')),
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_id VARCHAR(100) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    UNIQUE(id,hospital_id)
);
CREATE UNIQUE INDEX uk_billing_service_name ON billing_services(hospital_id,lower(btrim(name)));
CREATE INDEX idx_billing_services_hospital ON billing_services(hospital_id,category,active,name,id);

CREATE TABLE billing_tariffs (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    hospital_id UUID NOT NULL,
    effective_from DATE NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('CDF','USD')),
    unit VARCHAR(20) NOT NULL CHECK (unit IN ('ACT','DAY','NIGHT','HOUR','PACKAGE')),
    unit_price NUMERIC(16,2) NOT NULL CHECK (unit_price>=0),
    reason VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_id VARCHAR(100) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    cancelled_at TIMESTAMPTZ,
    cancelled_by VARCHAR(150),
    cancellation_reason VARCHAR(1000),
    FOREIGN KEY(service_id,hospital_id) REFERENCES billing_services(id,hospital_id),
    UNIQUE(id,service_id,hospital_id)
);
CREATE UNIQUE INDEX uk_billing_tariff_start ON billing_tariffs(service_id,currency,effective_from) WHERE cancelled_at IS NULL;
CREATE INDEX idx_billing_tariff_lookup ON billing_tariffs(service_id,currency,effective_from DESC) WHERE cancelled_at IS NULL;

CREATE TABLE billing_passage_charges (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    hospital_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    patient_code VARCHAR(50) NOT NULL,
    passage_id UUID NOT NULL,
    passage_code VARCHAR(50) NOT NULL,
    service_id UUID NOT NULL,
    tariff_id UUID NOT NULL,
    service_code VARCHAR(32) NOT NULL,
    service_name VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    performed_on DATE NOT NULL,
    quantity NUMERIC(9,3) NOT NULL CHECK (quantity>0),
    unit_price NUMERIC(16,2) NOT NULL CHECK (unit_price>=0),
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('CDF','USD')),
    total_amount NUMERIC(16,2) NOT NULL CHECK (total_amount>=0),
    source_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (source_type='MANUAL'),
    request_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNBILLED' CHECK (status IN ('UNBILLED','CANCELLED')),
    note VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_id VARCHAR(100) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    cancelled_at TIMESTAMPTZ,
    cancelled_by_id VARCHAR(100),
    cancelled_by VARCHAR(150),
    cancellation_reason VARCHAR(1000),
    UNIQUE(hospital_id,request_id),
    FOREIGN KEY(tariff_id,service_id,hospital_id) REFERENCES billing_tariffs(id,service_id,hospital_id),
    CHECK (total_amount=round(quantity*unit_price,2))
);
CREATE INDEX idx_billing_charges_passage ON billing_passage_charges(hospital_id,passage_id,status,performed_on DESC,id);

CREATE TABLE billing_catalog_events (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL,
    service_id UUID NOT NULL REFERENCES billing_services(id),
    kind VARCHAR(30) NOT NULL,
    details JSONB NOT NULL,
    operator_id VARCHAR(100) NOT NULL,
    operator_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_billing_catalog_events ON billing_catalog_events(service_id,created_at DESC,id);
