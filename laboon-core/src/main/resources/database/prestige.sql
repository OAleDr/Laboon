CREATE TABLE IF NOT EXISTS laboon_progression_prestige
(
    player_uuid UUID PRIMARY KEY,
    prestige INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT laboon_progression_prestige_non_negative CHECK (prestige >= 0),
    CONSTRAINT laboon_progression_prestige_player_fk
        FOREIGN KEY (player_uuid)
        REFERENCES "accounts"("uniqueId")
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_laboon_progression_prestige_value
    ON laboon_progression_prestige(prestige);
