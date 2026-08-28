CREATE TABLE hospital_laboratories (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    hospital_id UUID NOT NULL,
    location VARCHAR(255),
    phone_number VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_hospital_laboratories PRIMARY KEY (id),
    CONSTRAINT UK_hospital_laboratories_code UNIQUE (code),
    CONSTRAINT FK_hospital_laboratories_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);
