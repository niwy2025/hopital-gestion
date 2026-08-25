CREATE TABLE roles (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_roles_id DEFAULT NEWID(),
    code NVARCHAR(50) NOT NULL,
    label NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_roles PRIMARY KEY (id),
    CONSTRAINT UK_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_permissions_id DEFAULT NEWID(),
    code NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NOT NULL,
    CONSTRAINT PK_permissions PRIMARY KEY (id),
    CONSTRAINT UK_permissions_code UNIQUE (code)
);

CREATE TABLE accounts (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_accounts_id DEFAULT NEWID(),
    username NVARCHAR(100) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    display_name NVARCHAR(255) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_accounts_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_accounts PRIMARY KEY (id),
    CONSTRAINT UK_accounts_username UNIQUE (username),
    CONSTRAINT UK_accounts_email UNIQUE (email)
);

CREATE TABLE role_permissions (
    role_id UNIQUEIDENTIFIER NOT NULL,
    permission_id UNIQUEIDENTIFIER NOT NULL,
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT FK_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE account_roles (
    account_id UNIQUEIDENTIFIER NOT NULL,
    role_id UNIQUEIDENTIFIER NOT NULL,
    CONSTRAINT PK_account_roles PRIMARY KEY (account_id, role_id),
    CONSTRAINT FK_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT FK_account_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO roles (code, label) VALUES
    (N'ADMIN', N'Administrateur'),
    (N'DOCTOR', N'Médecin'),
    (N'NURSE', N'Infirmier'),
    (N'RECEPTIONIST', N'Accueil'),
    (N'PATIENT', N'Patient');

INSERT INTO permissions (code, description) VALUES
    (N'ACCOUNT_READ', N'Consulter les comptes'),
    (N'ACCOUNT_WRITE', N'Créer et modifier les comptes'),
    (N'ROLE_ASSIGN', N'Attribuer les rôles'),
    (N'PATIENT_READ', N'Consulter les dossiers patients'),
    (N'PRESCRIPTION_WRITE', N'Créer des prescriptions'),
    (N'CARE_WRITE', N'Saisir les actes de soin'),
    (N'APPOINTMENT_WRITE', N'Gérer les rendez-vous'),
    (N'PATIENT_REGISTER', N'Enregistrer les patients'),
    (N'PROFILE_READ', N'Consulter son profil'),
    (N'APPOINTMENT_READ', N'Consulter ses rendez-vous');

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    (N'ADMIN', N'ACCOUNT_READ'),
    (N'ADMIN', N'ACCOUNT_WRITE'),
    (N'ADMIN', N'ROLE_ASSIGN'),
    (N'DOCTOR', N'PATIENT_READ'),
    (N'DOCTOR', N'PRESCRIPTION_WRITE'),
    (N'NURSE', N'PATIENT_READ'),
    (N'NURSE', N'CARE_WRITE'),
    (N'RECEPTIONIST', N'APPOINTMENT_WRITE'),
    (N'RECEPTIONIST', N'PATIENT_REGISTER'),
    (N'PATIENT', N'PROFILE_READ'),
    (N'PATIENT', N'APPOINTMENT_READ')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code;
