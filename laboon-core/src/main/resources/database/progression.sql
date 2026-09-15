CREATE TABLE IF NOT EXISTS laboon_progression
(
    player_uuid UUID NOT NULL,
    game VARCHAR(32) NOT NULL,
    experience BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT laboon_progression_pk
        PRIMARY KEY (player_uuid, game),

    CONSTRAINT laboon_progression_experience_non_negative
        CHECK (experience >= 0),

    CONSTRAINT laboon_progression_player_fk
        FOREIGN KEY (player_uuid)
        REFERENCES "accounts"("uniqueId")
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_player
    ON laboon_progression(player_uuid);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_game
    ON laboon_progression(game);
