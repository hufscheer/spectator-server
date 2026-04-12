ALTER TABLE timelines ADD COLUMN assist_lineup_player_id BIGINT NULL;
ALTER TABLE timelines ADD CONSTRAINT fk_timelines_assist_player
    FOREIGN KEY (assist_lineup_player_id) REFERENCES lineup_players(id) ON DELETE CASCADE;