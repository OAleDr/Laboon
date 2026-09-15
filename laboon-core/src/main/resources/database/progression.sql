CREATE TABLE IF NOT EXISTS laboon_progression_experience
(
    player_uuid UUID NOT NULL,
    game VARCHAR(32) NOT NULL,
    experience BIGINT NOT NULL DEFAULT 0,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (
        player_uuid,
        game
    ),

    CONSTRAINT laboon_progression_experience_non_negative
        CHECK (experience >= 0),

    CONSTRAINT laboon_progression_experience_player_fk
        FOREIGN KEY (player_uuid)
        REFERENCES "accounts"("uniqueId")
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS laboon_progression_history
(
    id UUID PRIMARY KEY,

    player_uuid UUID NOT NULL,

    game VARCHAR(32) NOT NULL,

    amount BIGINT NOT NULL,

    source VARCHAR(64) NOT NULL,

    metadata TEXT,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT laboon_progression_history_amount_positive
        CHECK (amount > 0),

    CONSTRAINT laboon_progression_history_player_fk
        FOREIGN KEY (player_uuid)
        REFERENCES "accounts"("uniqueId")
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_history_player
    ON laboon_progression_history(player_uuid);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_history_game
    ON laboon_progression_history(player_uuid, game);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_history_created
    ON laboon_progression_history(created_at);
