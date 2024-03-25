ALTER TABLE lineup_players
    ADD COLUMN league_team_player_id BIGINT NOT NULL;

ALTER TABLE lineup_players
    ADD CONSTRAINT FK_LINEUP_PLAYERS_ON_LEAGUE_TEAM_PLAYER FOREIGN KEY (league_team_player_id) REFERENCES league_team_players (id);

