CREATE TABLE pending_cheer_talks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    destination VARCHAR(255) NOT NULL,
    cheer_talk JSON NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id)
);

-- created_at ASC 조회 최적화를 위한 인덱스
CREATE INDEX idx_pending_cheer_talks_created_at
    ON pending_cheer_talks (created_at ASC);
