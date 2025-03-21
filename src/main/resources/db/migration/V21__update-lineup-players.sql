ALTER TABLE lineup_players
    ADD COLUMN replaced_player_id BIGINT NULL;

ALTER TABLE lineup_players
    ADD CONSTRAINT FK_LINEUP_PLAYERS_ON_ITSELF FOREIGN KEY (replaced_player_id) REFERENCES lineup_players (id);