CREATE TABLE reference_laboratories (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    province_id BIGINT NOT NULL,
    address NVARCHAR(255) NULL,
    phone_number NVARCHAR(30) NULL,
    active BIT NOT NULL CONSTRAINT DF_reference_laboratories_active DEFAULT 1,
    CONSTRAINT PK_reference_laboratories PRIMARY KEY (id),
    CONSTRAINT UK_reference_laboratories_code UNIQUE (code),
    CONSTRAINT FK_reference_laboratories_province FOREIGN KEY (province_id) REFERENCES provinces (id)
);

CREATE TABLE laboratory_structures (
    id UNIQUEIDENTIFIER NOT NULL,
    code NVARCHAR(30) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    reference_laboratory_id UNIQUEIDENTIFIER NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_laboratory_structures_active DEFAULT 1,
    CONSTRAINT PK_laboratory_structures PRIMARY KEY (id),
    CONSTRAINT UK_laboratory_structures_code UNIQUE (code),
    CONSTRAINT FK_laboratory_structures_reference_laboratory
        FOREIGN KEY (reference_laboratory_id) REFERENCES reference_laboratories (id)
);
