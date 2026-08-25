CREATE TABLE health_areas (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_health_areas_id DEFAULT NEWID(),
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    health_zone_id UNIQUEIDENTIFIER NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_health_areas_active DEFAULT 1,
    CONSTRAINT PK_health_areas PRIMARY KEY (id),
    CONSTRAINT UK_health_areas_code UNIQUE (code),
    CONSTRAINT FK_health_areas_health_zone FOREIGN KEY (health_zone_id) REFERENCES health_zones (id)
);
GO

ALTER TABLE hospitals ADD health_area_id UNIQUEIDENTIFIER NULL;
GO

ALTER TABLE hospitals
    ADD CONSTRAINT FK_hospitals_health_area FOREIGN KEY (health_area_id) REFERENCES health_areas (id);
GO

CREATE INDEX IX_health_areas_health_zone_id ON health_areas (health_zone_id);
CREATE INDEX IX_hospitals_health_area_id ON hospitals (health_area_id);
