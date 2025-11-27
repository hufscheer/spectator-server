ALTER TABLE timelines
    ADD COLUMN game_team1_id BIGINT NULL;

ALTER TABLE timelines
    ADD COLUMN game_team2_id BIGINT NULL;

ALTER TABLE timelines
    ADD COLUMN snapshot_score1 INT NULL;

ALTER TABLE timelines
    ADD COLUMN snapshot_score2 INT NULL;

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_GAME_TEAM1 FOREIGN KEY (game_team1_id) REFERENCES game_teams (id);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_GAME_TEAM2 FOREIGN KEY (game_team2_id) REFERENCES game_teams (id);
