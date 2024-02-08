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
    score            INT                   NOT NULL
);

CREATE TABLE replacement_records
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    record_id                 BIGINT                NOT NULL,
    origin_lineup_player_id   BIGINT                NOT NULL,
    replaced_lineup_player_id BIGINT                NOT NULL
);

ALTER TABLE league_team_players
    ADD COLUMN number INT;

ALTER TABLE lineup_players
    ADD COLUMN number INT;

ALTER TABLE lineup_players
    ADD COLUMN is_captain BOOLEAN;