CREATE TABLE health_areas (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    health_zone_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_health_areas PRIMARY KEY (id),
    CONSTRAINT UK_health_areas_code UNIQUE (code),
    CONSTRAINT FK_health_areas_health_zone FOREIGN KEY (health_zone_id) REFERENCES health_zones (id)
);

ALTER TABLE hospitals ADD COLUMN health_area_id UUID;

ALTER TABLE hospitals
    ADD CONSTRAINT FK_hospitals_health_area FOREIGN KEY (health_area_id) REFERENCES health_areas (id);

CREATE INDEX IX_health_areas_health_zone_id ON health_areas (health_zone_id);
CREATE INDEX IX_hospitals_health_area_id ON hospitals (health_area_id);
