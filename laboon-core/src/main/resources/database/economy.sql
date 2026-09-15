CREATE TABLE IF NOT EXISTS laboon_economy_accounts
(
    player_uuid
    UUID
    PRIMARY
    KEY,
    coins
    BIGINT
    NOT
    NULL
    DEFAULT
    0,
    tokens
    BIGINT
    NOT
    NULL
    DEFAULT
    0,
    updated_at
    TIMESTAMP
    WITH
    TIME
    ZONE
    NOT
    NULL
    DEFAULT
    CURRENT_TIMESTAMP,
    CONSTRAINT
    laboon_economy_accounts_coins_non_negative
    CHECK
(
    coins
    >=
    0
), CONSTRAINT laboon_economy_accounts_tokens_non_negative CHECK
(
    tokens
    >=
    0
) );
CREATE TABLE IF NOT EXISTS laboon_economy_transactions
(
    id
    UUID
    PRIMARY
    KEY,
    player_uuid
    UUID
    NOT
    NULL,
    currency
    VARCHAR
(
    32
) NOT NULL, amount BIGINT NOT NULL, type VARCHAR
(
    32
) NOT NULL, source VARCHAR
(
    128
), metadata TEXT, created_at TIMESTAMP
    WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT laboon_economy_transactions_amount_positive CHECK (amount > 0), CONSTRAINT laboon_economy_transactions_currency_check CHECK
(
    currency
    IN
(
    'COINS',
    'TOKENS'
) ), CONSTRAINT laboon_economy_transactions_type_check CHECK
(
    type
    IN
(
    'DEPOSIT',
    'WITHDRAW',
    'TRANSFER',
    'REWARD',
    'PURCHASE',
    'ADMIN'
) ), CONSTRAINT laboon_economy_transactions_player_fk FOREIGN KEY
(
    player_uuid
) REFERENCES laboon_economy_accounts
(
    player_uuid
)
    ON DELETE CASCADE );
CREATE INDEX IF NOT EXISTS idx_laboon_economy_transactions_player ON laboon_economy_transactions(player_uuid);
CREATE INDEX IF NOT EXISTS idx_laboon_economy_transactions_player_created ON laboon_economy_transactions(player_uuid, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_laboon_economy_transactions_source ON laboon_economy_transactions(source);