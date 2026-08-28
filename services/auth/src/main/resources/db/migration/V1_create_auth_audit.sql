CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE auth_login_audits (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    account_id UUID NULL,
    username VARCHAR(100) NOT NULL,
    user_agent VARCHAR(1024) NOT NULL,
    status VARCHAR(30) NOT NULL,
    access_token_expires_at TIMESTAMPTZ NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_login_audits PRIMARY KEY (id)
);
