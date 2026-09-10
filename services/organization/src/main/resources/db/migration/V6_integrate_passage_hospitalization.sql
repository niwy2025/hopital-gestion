ALTER TABLE patrimony_bed_stays
    ADD COLUMN occupied_at TIMESTAMPTZ,
    ADD COLUMN confirmed_by VARCHAR(150),
    ADD COLUMN release_note VARCHAR(1000),
    ADD COLUMN release_reason VARCHAR(30),
    ADD COLUMN bed_code VARCHAR(30),
    ADD COLUMN bed_name VARCHAR(200),
    ADD COLUMN room_id UUID,
    ADD COLUMN room_name VARCHAR(200),
    ADD COLUMN building_name VARCHAR(200),
    ADD COLUMN service_name VARCHAR(150),
    ADD COLUMN patient_name VARCHAR(450),
    ADD COLUMN legacy_record BOOLEAN NOT NULL DEFAULT FALSE;

-- Conserver les traces existantes, sans inventer une heure d’occupation pour
-- les anciennes réservations déjà libérées.
UPDATE patrimony_bed_stays s SET
    bed_code=a.code, bed_name=a.name, room_id=l.id, room_name=l.name,
    building_name=COALESCE(grandparent.name,parent.name),service_name=l.service_name,
    legacy_record=TRUE
FROM patrimony_assets a
LEFT JOIN patrimony_locations l ON l.id=a.location_id
LEFT JOIN patrimony_locations parent ON parent.id=l.parent_id
LEFT JOIN patrimony_locations grandparent ON grandparent.id=parent.parent_id
WHERE s.asset_id=a.id;
CREATE INDEX idx_patrimony_stays_passage_history ON patrimony_bed_stays(passage_id,started_at DESC,id);
CREATE INDEX idx_patrimony_stays_pending_check ON patrimony_bed_stays(checked_at) WHERE status<>'RELEASED';
