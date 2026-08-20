ALTER TABLE team_players
    ADD COLUMN position VARCHAR(255) NULL;

ALTER TABLE lineup_players
    ADD COLUMN position VARCHAR(255) NULL;
