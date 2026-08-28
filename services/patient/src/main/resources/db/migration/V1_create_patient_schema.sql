CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE patients (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone_number VARCHAR(30),
    address VARCHAR(255),
    registration_hospital_code VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_patients PRIMARY KEY (id),
    CONSTRAINT UK_patients_code UNIQUE (code)
);
