CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS accounts (
                                        id BIGSERIAL PRIMARY KEY,

                                        uuid UUID NOT NULL UNIQUE,

                                        name VARCHAR(16) NOT NULL,

    group_name VARCHAR(32) NOT NULL,

    tag VARCHAR(32) NOT NULL,

    experience BIGINT NOT NULL DEFAULT 0,

    type VARCHAR(32) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_login TIMESTAMP WITH TIME ZONE,

                             language VARCHAR(16) NOT NULL DEFAULT 'pt_BR',

    private_messages BOOLEAN NOT NULL DEFAULT TRUE,

    friend_requests BOOLEAN NOT NULL DEFAULT TRUE,

    server_join_messages BOOLEAN NOT NULL DEFAULT TRUE,

    updated_at TIMESTAMP WITH TIME ZONE
                             NOT NULL DEFAULT CURRENT_TIMESTAMP
                             );

CREATE UNIQUE INDEX IF NOT EXISTS
    idx_accounts_name_lower
    ON accounts (LOWER(name));

CREATE INDEX IF NOT EXISTS
    idx_accounts_experience
    ON accounts (experience DESC);

CREATE TABLE IF NOT EXISTS account_temporary_groups (

                                                        id BIGSERIAL PRIMARY KEY,

                                                        account_id BIGINT NOT NULL
                                                        REFERENCES accounts(id)
    ON DELETE CASCADE,

    group_name VARCHAR(32) NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE
        NOT NULL,

        UNIQUE(account_id, group_name)
    );

CREATE INDEX IF NOT EXISTS
    idx_temp_groups_account
    ON account_temporary_groups(account_id);

CREATE INDEX IF NOT EXISTS
    idx_temp_groups_expiration
    ON account_temporary_groups(expires_at);

CREATE TABLE IF NOT EXISTS punishments (

                                           id BIGSERIAL PRIMARY KEY,

                                           account_id BIGINT NOT NULL
                                           REFERENCES accounts(id)
    ON DELETE CASCADE,

    type VARCHAR(16) NOT NULL,

    applied_by VARCHAR(16) NOT NULL,

    applied_by_uuid UUID NOT NULL,

    ip VARCHAR(64),

    server VARCHAR(64),

    reason TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
    NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE,

        revoked BOOLEAN NOT NULL DEFAULT FALSE,

        revoked_by VARCHAR(16),

    revoked_by_uuid UUID,

    revoked_at TIMESTAMP WITH TIME ZONE
        );

CREATE INDEX IF NOT EXISTS
    idx_punishments_account
    ON punishments(account_id);

CREATE INDEX IF NOT EXISTS
    idx_punishments_type
    ON punishments(type);

CREATE INDEX IF NOT EXISTS
    idx_punishments_expiration
    ON punishments(expires_at);