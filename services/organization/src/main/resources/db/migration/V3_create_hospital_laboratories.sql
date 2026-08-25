CREATE TABLE hospital_laboratories (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_hospital_laboratories_id DEFAULT NEWID(),
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    hospital_id UNIQUEIDENTIFIER NOT NULL,
    location NVARCHAR(255) NULL,
    phone_number NVARCHAR(30) NULL,
    active BIT NOT NULL CONSTRAINT DF_hospital_laboratories_active DEFAULT 1,
    CONSTRAINT PK_hospital_laboratories PRIMARY KEY (id),
    CONSTRAINT UK_hospital_laboratories_code UNIQUE (code),
    CONSTRAINT FK_hospital_laboratories_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);
