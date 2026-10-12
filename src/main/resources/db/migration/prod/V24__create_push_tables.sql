-- 푸시 알림 기기와 팔로우(팀/경기). 관객은 로그인이 없어 기기 단위로 구독을 들고 있는다.
-- token_hash: 토큰은 대소문자를 구분하는데 MySQL 유니크 인덱스는 구분하지 않아 SHA-256 해시로 유일성을 건다
CREATE TABLE push_devices
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    device_key   VARCHAR(36)           NOT NULL,
    token        VARCHAR(512)          NOT NULL,
    token_hash   CHAR(64)              NOT NULL,
    platform     VARCHAR(16)           NOT NULL,
    created_at   DATETIME(6)           NOT NULL,
    last_seen_at DATETIME(6)           NOT NULL,

    CONSTRAINT pk_push_devices PRIMARY KEY (id),
    CONSTRAINT uc_push_devices_device_key UNIQUE (device_key),
    CONSTRAINT uc_push_devices_token_hash UNIQUE (token_hash)
);

CREATE TABLE push_follows
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    device_id   BIGINT                NOT NULL,
    target_type VARCHAR(16)           NOT NULL,
    target_id   BIGINT                NOT NULL,

    CONSTRAINT pk_push_follows PRIMARY KEY (id),
    CONSTRAINT fk_push_follows_on_push_devices FOREIGN KEY (device_id) REFERENCES push_devices (id) ON DELETE CASCADE,
    CONSTRAINT uc_push_follows_device_target UNIQUE (device_id, target_type, target_id)
);

CREATE INDEX idx_push_follows_target ON push_follows (target_type, target_id);
