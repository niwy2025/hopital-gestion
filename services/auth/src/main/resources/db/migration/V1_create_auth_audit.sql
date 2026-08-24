CREATE TABLE auth_login_audits (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT DF_auth_login_audits_id DEFAULT NEWID(),
    account_id UNIQUEIDENTIFIER NULL,
    username NVARCHAR(100) NOT NULL,
    user_agent NVARCHAR(1024) NOT NULL,
    status NVARCHAR(30) NOT NULL,
    access_token_expires_at DATETIME2 NULL,
    occurred_at DATETIME2 NOT NULL CONSTRAINT DF_auth_login_audits_occurred_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_auth_login_audits PRIMARY KEY (id)
);
