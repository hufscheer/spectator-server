CREATE TABLE team_players
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    team_id       BIGINT                NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` VARCHAR(255)          NULL,
    CONSTRAINT pk_team_players PRIMARY KEY (id)
);

ALTER TABLE team_players
    ADD CONSTRAINT FK_TEAM_PLAYERS_ON_TEAM FOREIGN KEY (team_id) REFERENCES teams (id);
