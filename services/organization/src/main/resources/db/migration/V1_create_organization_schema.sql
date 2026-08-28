CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE provinces (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_provinces PRIMARY KEY (id),
    CONSTRAINT UK_provinces_code UNIQUE (code)
);

CREATE TABLE health_zones (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    province_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_health_zones PRIMARY KEY (id),
    CONSTRAINT UK_health_zones_code UNIQUE (code),
    CONSTRAINT FK_health_zones_province FOREIGN KEY (province_id) REFERENCES provinces (id)
);

CREATE TABLE hospitals (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(30) NOT NULL,
    health_zone_id UUID NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_hospitals PRIMARY KEY (id),
    CONSTRAINT UK_hospitals_code UNIQUE (code),
    CONSTRAINT FK_hospitals_health_zone FOREIGN KEY (health_zone_id) REFERENCES health_zones (id)
);
