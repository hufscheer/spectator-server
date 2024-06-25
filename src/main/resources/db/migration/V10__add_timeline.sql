CREATE TABLE timelines
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    type                      VARCHAR(31)           NULL,
    game_id                   BIGINT                NOT NULL,
    recorded_quarter_id       BIGINT                NOT NULL,
    recorded_at               INT                   NOT NULL,
    scorer_id                 BIGINT                NULL,
    score                     INT                   NULL,
    is_success                BIT(1)                NULL,
    origin_lineup_player_id   BIGINT                NULL,
    replaced_lineup_player_id BIGINT                NULL,
    game_progress_type        VARCHAR(255)          NULL,
    CONSTRAINT pk_timelines PRIMARY KEY (id)
);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_GAME FOREIGN KEY (game_id) REFERENCES games (id);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_ORIGIN_LINEUP_PLAYER FOREIGN KEY (origin_lineup_player_id) REFERENCES lineup_players (id);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_RECORDED_QUARTER FOREIGN KEY (recorded_quarter_id) REFERENCES quarters (id);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_REPLACED_LINEUP_PLAYER FOREIGN KEY (replaced_lineup_player_id) REFERENCES lineup_players (id);

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_SCORER FOREIGN KEY (scorer_id) REFERENCES lineup_players (id);
