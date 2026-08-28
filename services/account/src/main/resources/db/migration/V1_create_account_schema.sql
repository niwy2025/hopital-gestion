CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE TABLE accounts (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uk_accounts_username UNIQUE (username),
    CONSTRAINT uk_accounts_email UNIQUE (email)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE account_roles (
    account_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_account_roles PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_account_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO roles (code, label) VALUES
    ('ADMIN', 'Administrateur'),
    ('DOCTOR', 'Médecin'),
    ('NURSE', 'Infirmier'),
    ('RECEPTIONIST', 'Accueil'),
    ('PATIENT', 'Patient');

INSERT INTO permissions (code, description) VALUES
    ('ACCOUNT_READ', 'Consulter les comptes'),
    ('ACCOUNT_WRITE', 'Créer et modifier les comptes'),
    ('ROLE_ASSIGN', 'Attribuer les rôles'),
    ('PATIENT_READ', 'Consulter les dossiers patients'),
    ('PRESCRIPTION_WRITE', 'Créer des prescriptions'),
    ('CARE_WRITE', 'Saisir les actes de soin'),
    ('APPOINTMENT_WRITE', 'Gérer les rendez-vous'),
    ('PATIENT_REGISTER', 'Enregistrer les patients'),
    ('PROFILE_READ', 'Consulter son profil'),
    ('APPOINTMENT_READ', 'Consulter ses rendez-vous');

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM (VALUES
    ('ADMIN', 'ACCOUNT_READ'),
    ('ADMIN', 'ACCOUNT_WRITE'),
    ('ADMIN', 'ROLE_ASSIGN'),
    ('DOCTOR', 'PATIENT_READ'),
    ('DOCTOR', 'PRESCRIPTION_WRITE'),
    ('NURSE', 'PATIENT_READ'),
    ('NURSE', 'CARE_WRITE'),
    ('RECEPTIONIST', 'APPOINTMENT_WRITE'),
    ('RECEPTIONIST', 'PATIENT_REGISTER'),
    ('PATIENT', 'PROFILE_READ'),
    ('PATIENT', 'APPOINTMENT_READ')
) AS role_permission_seed (role_code, permission_code)
JOIN roles AS role ON role.code = role_permission_seed.role_code
JOIN permissions AS permission ON permission.code = role_permission_seed.permission_code;
