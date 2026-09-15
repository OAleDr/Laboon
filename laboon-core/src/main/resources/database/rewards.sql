CREATE TABLE IF NOT EXISTS laboon_reward_claims
(
    reward_id VARCHAR(128) PRIMARY KEY,

    player_uuid UUID NOT NULL,

    source VARCHAR(64) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
                             NOT NULL
                             DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT laboon_reward_claims_player_fk
                             FOREIGN KEY (player_uuid)
    REFERENCES laboon_economy_accounts(player_uuid)
                         ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_laboon_reward_claims_player
    ON laboon_reward_claims(player_uuid);

CREATE INDEX IF NOT EXISTS idx_laboon_reward_claims_source
    ON laboon_reward_claims(source);