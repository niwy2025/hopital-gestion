CREATE TABLE patients (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender NVARCHAR(20) NOT NULL,
    phone_number NVARCHAR(30) NULL,
    address NVARCHAR(255) NULL,
    registration_hospital_code NVARCHAR(30) NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_patients_active DEFAULT 1,
    created_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT PK_patients PRIMARY KEY (id),
    CONSTRAINT UK_patients_code UNIQUE (code)
);
