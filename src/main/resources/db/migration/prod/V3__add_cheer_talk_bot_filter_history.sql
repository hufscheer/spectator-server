CREATE TABLE cheer_talk_bot_filter_history (
    id BIGINT NOT NULL AUTO_INCREMENT,

    cheer_talk_id BIGINT NULL,

    filtered_at DATETIME(6) NOT NULL,

    cheer_talk_filter_result VARCHAR(50) NOT NULL,

    bot_type VARCHAR(50) NOT NULL,

    raw_bot_response JSON NOT NULL,

    latency_ms INT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_cheer_talk_bot_filter_history_cheer_talk
        FOREIGN KEY (cheer_talk_id)
        REFERENCES cheer_talk (id)
        ON DELETE SET NULL
);