CREATE TABLE provinces (
    id BIGINT IDENTITY(1, 1) NOT NULL,
    code NVARCHAR(20) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_provinces_active DEFAULT 1,
    CONSTRAINT PK_provinces PRIMARY KEY (id),
    CONSTRAINT UK_provinces_code UNIQUE (code)
);

CREATE TABLE health_zones (
    id BIGINT IDENTITY(1, 1) NOT NULL,
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    province_id BIGINT NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_health_zones_active DEFAULT 1,
    CONSTRAINT PK_health_zones PRIMARY KEY (id),
    CONSTRAINT UK_health_zones_code UNIQUE (code),
    CONSTRAINT FK_health_zones_province FOREIGN KEY (province_id) REFERENCES provinces (id)
);

CREATE TABLE hospitals (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    type NVARCHAR(30) NOT NULL,
    health_zone_id BIGINT NOT NULL,
    address NVARCHAR(255) NULL,
    phone_number NVARCHAR(30) NULL,
    active BIT NOT NULL CONSTRAINT DF_hospitals_active DEFAULT 1,
    CONSTRAINT PK_hospitals PRIMARY KEY (id),
    CONSTRAINT UK_hospitals_code UNIQUE (code),
    CONSTRAINT FK_hospitals_health_zone FOREIGN KEY (health_zone_id) REFERENCES health_zones (id)
);
