CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE personnel (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    employee_number VARCHAR(40) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    job_title VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(255),
    address VARCHAR(255),
    hospital_id UUID,
    account_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT PK_personnel PRIMARY KEY (id),
    CONSTRAINT UK_personnel_employee_number UNIQUE (employee_number)
);

CREATE UNIQUE INDEX UX_personnel_account_id
    ON personnel(account_id)
    WHERE account_id IS NOT NULL;
