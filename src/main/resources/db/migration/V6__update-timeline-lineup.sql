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

ALTER TABLE replacement_records
    ADD CONSTRAINT FK_RECORDS_IN_REPLACEMENT_RECORDS FOREIGN KEY (record_id) REFERENCES records (id);

ALTER TABLE replacement_records
    ADD CONSTRAINT FK_ORIGIN_LINEUP_PLAYER FOREIGN KEY (origin_lineup_player_id) REFERENCES lineup_players (id);

ALTER TABLE replacement_records
    ADD CONSTRAINT FK_REPLACED_LINEUP_PLAYER FOREIGN KEY (replaced_lineup_player_id) REFERENCES lineup_players (id);

ALTER TABLE league_team_players
    ADD COLUMN number INT NULL;

ALTER TABLE lineup_players
    ADD COLUMN number INT;

ALTER TABLE lineup_players
    ADD COLUMN is_captain BOOLEAN;

ALTER TABLE records
    DROP COLUMN lineup_player_id;

ALTER TABLE records
    DROP COLUMN score;