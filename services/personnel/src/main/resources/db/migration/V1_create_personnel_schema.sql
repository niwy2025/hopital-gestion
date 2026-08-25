CREATE TABLE personnel (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_personnel_id DEFAULT NEWID(),
    employee_number NVARCHAR(40) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    middle_name NVARCHAR(100) NULL,
    date_of_birth DATE NULL,
    gender NVARCHAR(20) NOT NULL,
    category NVARCHAR(30) NOT NULL,
    job_title NVARCHAR(150) NOT NULL,
    phone_number NVARCHAR(30) NULL,
    email NVARCHAR(255) NULL,
    address NVARCHAR(255) NULL,
    hospital_id UNIQUEIDENTIFIER NULL,
    account_id UNIQUEIDENTIFIER NULL,
    active BIT NOT NULL CONSTRAINT DF_personnel_active DEFAULT 1,
    created_at DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT PK_personnel PRIMARY KEY (id),
    CONSTRAINT UK_personnel_employee_number UNIQUE (employee_number)
);

CREATE UNIQUE INDEX UX_personnel_account_id
    ON personnel(account_id)
    WHERE account_id IS NOT NULL;
