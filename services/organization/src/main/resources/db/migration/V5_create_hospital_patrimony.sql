CREATE TABLE patrimony_categories (
    id UUID PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO patrimony_categories(id,code,name) VALUES
    (gen_random_uuid(),'BUILDING','Bâtiments et installations'),
    (gen_random_uuid(),'BED','Lits et berceaux'),
    (gen_random_uuid(),'BEDDING','Matelas et accessoires de couchage'),
    (gen_random_uuid(),'MOBILITY','Fauteuils roulants, brancards et mobilité'),
    (gen_random_uuid(),'FURNITURE','Mobilier et rangement'),
    (gen_random_uuid(),'CONSULTATION','Consultation et triage'),
    (gen_random_uuid(),'EMERGENCY','Soins et urgences'),
    (gen_random_uuid(),'MATERNITY','Maternité'),
    (gen_random_uuid(),'LABORATORY','Matériel de laboratoire'),
    (gen_random_uuid(),'COLD_CHAIN','Vaccination et chaîne du froid'),
    (gen_random_uuid(),'HYGIENE','Stérilisation et hygiène'),
    (gen_random_uuid(),'IT','Informatique et administration'),
    (gen_random_uuid(),'UTILITIES','Énergie, eau et sécurité'),
    (gen_random_uuid(),'TRANSPORT','Transport et logistique'),
    (gen_random_uuid(),'SPECIALIZED','Équipements spécialisés'),
    (gen_random_uuid(),'OTHER','Autres biens durables');

CREATE TABLE patrimony_locations (
    id UUID PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id), parent_id UUID REFERENCES patrimony_locations(id),
    kind VARCHAR(20) NOT NULL CHECK(kind IN ('BUILDING','FLOOR','ROOM')),
    name VARCHAR(200) NOT NULL, service_name VARCHAR(150), active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by VARCHAR(150) NOT NULL,
    UNIQUE(hospital_id,id)
);
CREATE INDEX idx_patrimony_locations_hospital ON patrimony_locations(hospital_id,kind,name);
CREATE TABLE patrimony_assets (
    id UUID PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id), category_code VARCHAR(40) NOT NULL REFERENCES patrimony_categories(code),
    name VARCHAR(200) NOT NULL, brand VARCHAR(100), model VARCHAR(100), serial_number VARCHAR(150),
    location_id UUID, responsible_personnel_id UUID, responsible_name VARCHAR(250),
    acquisition_source VARCHAR(20) NOT NULL CHECK(acquisition_source IN ('PURCHASE','DONATION','PROVISION','EXISTING')),
    owner_name VARCHAR(200), supplier_name VARCHAR(200), received_on DATE NOT NULL, commissioned_on DATE,
    purchase_cost NUMERIC(19,2) CHECK(purchase_cost >= 0), currency VARCHAR(3) NOT NULL CHECK(currency IN ('CDF','USD')),
    warranty_until DATE, next_maintenance_on DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK(status IN ('AVAILABLE','FAULTY','MAINTENANCE','LOANED','RETIRED','LOST')),
    cleaning_required BOOLEAN NOT NULL DEFAULT FALSE, loan_recipient VARCHAR(200), loan_due_on DATE,
    notes VARCHAR(4000), version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by_id VARCHAR(100) NOT NULL, created_by VARCHAR(150) NOT NULL,
    FOREIGN KEY(hospital_id,location_id) REFERENCES patrimony_locations(hospital_id,id)
);
CREATE INDEX idx_patrimony_assets_hospital ON patrimony_assets(hospital_id,category_code,status,name);
CREATE INDEX idx_patrimony_assets_location ON patrimony_assets(location_id);
CREATE TABLE patrimony_events (
    id UUID PRIMARY KEY, asset_id UUID REFERENCES patrimony_assets(id), location_id UUID REFERENCES patrimony_locations(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id), kind VARCHAR(50) NOT NULL, note VARCHAR(4000) NOT NULL,
    operator_id VARCHAR(100) NOT NULL, operator_name VARCHAR(150) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_patrimony_events_asset ON patrimony_events(asset_id,created_at DESC);
CREATE INDEX idx_patrimony_events_hospital ON patrimony_events(hospital_id,created_at DESC);
CREATE TABLE patrimony_operations (
    id UUID PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE, asset_id UUID NOT NULL REFERENCES patrimony_assets(id),
    kind VARCHAR(20) NOT NULL CHECK(kind IN ('MAINTENANCE','RETIREMENT','LOSS')),
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK(status IN ('REQUESTED','IN_PROGRESS','COMPLETED','REJECTED')),
    reason VARCHAR(2000) NOT NULL, planned_on DATE, result_note VARCHAR(4000), cost NUMERIC(19,2) CHECK(cost >= 0),
    requested_by_id VARCHAR(100) NOT NULL, requested_by VARCHAR(150) NOT NULL, requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_by VARCHAR(150), completed_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX uk_patrimony_active_operation ON patrimony_operations(asset_id) WHERE status IN ('REQUESTED','IN_PROGRESS');
CREATE TABLE patrimony_documents (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL REFERENCES patrimony_assets(id),
    kind VARCHAR(30) NOT NULL CHECK(kind IN ('PHOTO','INVOICE','MANUAL','WARRANTY','REPORT','OTHER')),
    file_name VARCHAR(200) NOT NULL, content_type VARCHAR(100) NOT NULL, content BYTEA NOT NULL,
    size_bytes INTEGER NOT NULL CHECK(size_bytes > 0 AND size_bytes <= 3145728),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by VARCHAR(150) NOT NULL
);
CREATE INDEX idx_patrimony_documents_asset ON patrimony_documents(asset_id,created_at DESC);
CREATE TABLE patrimony_bed_stays (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL REFERENCES patrimony_assets(id),
    passage_id UUID NOT NULL, passage_code VARCHAR(40) NOT NULL, patient_code VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN ('RESERVED','OCCUPIED','RELEASED')),
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(), started_by VARCHAR(150) NOT NULL,
    ended_at TIMESTAMPTZ, ended_by VARCHAR(150), note VARCHAR(1000), checked_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX uk_patrimony_bed_active ON patrimony_bed_stays(asset_id) WHERE status <> 'RELEASED';
CREATE UNIQUE INDEX uk_patrimony_passage_bed ON patrimony_bed_stays(passage_id) WHERE status <> 'RELEASED';
CREATE TABLE patrimony_accounting_outbox (
    id UUID PRIMARY KEY, asset_id UUID NOT NULL UNIQUE REFERENCES patrimony_assets(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', attempts INTEGER NOT NULL DEFAULT 0,
    retry_at TIMESTAMPTZ NOT NULL DEFAULT now(), delivered_at TIMESTAMPTZ
);
