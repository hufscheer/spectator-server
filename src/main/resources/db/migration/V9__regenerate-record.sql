DROP TABLE IF EXISTS score_records;
DROP TABLE IF EXISTS replacement_records;
DROP TABLE IF EXISTS records;

CREATE TABLE records
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    game_id          BIGINT                NULL,
    game_team_id     BIGINT                NULL,
    lineup_player_id BIGINT                NULL,
    score            INT                   NOT NULL,
    scored_quarter   VARCHAR(255)          NOT NULL,
    scored_at        datetime              NOT NULL,
    CONSTRAINT pk_records PRIMARY KEY (id)
);

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_GAME FOREIGN KEY (game_id) REFERENCES games (id);

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_GAME_TEAM FOREIGN KEY (game_team_id) REFERENCES game_teams (id);

ALTER TABLE records
    DROP COLUMN scored_quarter;
ALTER TABLE records
    ADD COLUMN scored_quarter_id BIGINT NOT NULL;

ALTER TABLE records
    MODIFY scored_at INT;

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_SCORED_QUARTER FOREIGN KEY (scored_quarter_id) REFERENCES quarters (id);

ALTER TABLE records
    RENAME COLUMN scored_quarter_id TO recorded_quarter_id;

ALTER TABLE records
    RENAME COLUMN scored_at TO recorded_at;

ALTER TABLE records
    ADD COLUMN record_type VARCHAR(255);

CREATE TABLE score_records
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    record_id        BIGINT                NOT NULL,
    lineup_player_id BIGINT                NOT NULL,
    score            INT                   NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE score_records
    ADD CONSTRAINT FK_RECORDS_IN_SCORE_RECORDS FOREIGN KEY (record_id) REFERENCES records (id);

ALTER TABLE score_records
    ADD CONSTRAINT FK_LINEUP_PLAYER_ID FOREIGN KEY (lineup_player_id) REFERENCES lineup_players (id);

CREATE TABLE replacement_records
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    record_id                 BIGINT                NOT NULL,
    origin_lineup_player_id   BIGINT                NOT NULL,
    replaced_lineup_player_id BIGINT                NOT NULL,
    PRIMARY KEY (id)
);
