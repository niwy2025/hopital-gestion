-- Les provinces et zones de santé sont des entités métier. Leurs identifiants
-- doivent être non prévisibles et cohérents avec les autres référentiels.

ALTER TABLE hospitals DROP CONSTRAINT FK_hospitals_health_zone;
ALTER TABLE reference_laboratories DROP CONSTRAINT FK_reference_laboratories_province;
ALTER TABLE health_zones DROP CONSTRAINT FK_health_zones_province;

ALTER TABLE provinces ADD uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE provinces SET uuid_id = NEWID();
ALTER TABLE provinces ALTER COLUMN uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE health_zones ADD uuid_id UNIQUEIDENTIFIER NULL;
ALTER TABLE health_zones ADD province_uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE health_zones SET uuid_id = NEWID();
UPDATE health_zone
SET province_uuid_id = province.uuid_id
FROM health_zones health_zone
INNER JOIN provinces province ON province.id = health_zone.province_id;
ALTER TABLE health_zones ALTER COLUMN uuid_id UNIQUEIDENTIFIER NOT NULL;
ALTER TABLE health_zones ALTER COLUMN province_uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE hospitals ADD health_zone_uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE hospital
SET health_zone_uuid_id = health_zone.uuid_id
FROM hospitals hospital
INNER JOIN health_zones health_zone ON health_zone.id = hospital.health_zone_id;
ALTER TABLE hospitals ALTER COLUMN health_zone_uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE reference_laboratories ADD province_uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE reference_laboratory
SET province_uuid_id = province.uuid_id
FROM reference_laboratories reference_laboratory
INNER JOIN provinces province ON province.id = reference_laboratory.province_id;
ALTER TABLE reference_laboratories ALTER COLUMN province_uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE provinces DROP CONSTRAINT PK_provinces;
ALTER TABLE health_zones DROP CONSTRAINT PK_health_zones;

ALTER TABLE hospitals DROP COLUMN health_zone_id;
ALTER TABLE reference_laboratories DROP COLUMN province_id;
ALTER TABLE health_zones DROP COLUMN province_id;
ALTER TABLE health_zones DROP COLUMN id;
ALTER TABLE provinces DROP COLUMN id;

EXEC sp_rename N'dbo.provinces.uuid_id', N'id', N'COLUMN';
EXEC sp_rename N'dbo.health_zones.uuid_id', N'id', N'COLUMN';
EXEC sp_rename N'dbo.health_zones.province_uuid_id', N'province_id', N'COLUMN';
EXEC sp_rename N'dbo.hospitals.health_zone_uuid_id', N'health_zone_id', N'COLUMN';
EXEC sp_rename N'dbo.reference_laboratories.province_uuid_id', N'province_id', N'COLUMN';
GO

ALTER TABLE provinces ADD CONSTRAINT PK_provinces PRIMARY KEY (id);
ALTER TABLE health_zones ADD CONSTRAINT PK_health_zones PRIMARY KEY (id);
ALTER TABLE health_zones ADD CONSTRAINT FK_health_zones_province
    FOREIGN KEY (province_id) REFERENCES provinces (id);
ALTER TABLE hospitals ADD CONSTRAINT FK_hospitals_health_zone
    FOREIGN KEY (health_zone_id) REFERENCES health_zones (id);
ALTER TABLE reference_laboratories ADD CONSTRAINT FK_reference_laboratories_province
    FOREIGN KEY (province_id) REFERENCES provinces (id);
