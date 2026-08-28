CREATE TABLE reference_laboratories (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    province_id UUID NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_reference_laboratories PRIMARY KEY (id),
    CONSTRAINT UK_reference_laboratories_code UNIQUE (code),
    CONSTRAINT FK_reference_laboratories_province FOREIGN KEY (province_id) REFERENCES provinces (id)
);

CREATE TABLE laboratory_structures (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    reference_laboratory_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_laboratory_structures PRIMARY KEY (id),
    CONSTRAINT UK_laboratory_structures_code UNIQUE (code),
    CONSTRAINT FK_laboratory_structures_reference_laboratory
        FOREIGN KEY (reference_laboratory_id) REFERENCES reference_laboratories (id)
);
