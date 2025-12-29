-- cheer_talks 테이블의 is_blocked(BOOLEAN) 컬럼을 block_status(ENUM) 컬럼으로 변경
-- ACTIVE: 차단되지 않은 상태
-- BLOCKED_BY_ADMIN: 관리자에 의해 차단된 상태
-- BLOCKED_BY_BOT: 봇에 의해 차단된 상태

ALTER TABLE cheer_talks
    ADD COLUMN block_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';

-- 기존 is_blocked 데이터를 block_status로 마이그레이션
UPDATE cheer_talks
SET block_status = CASE
                       WHEN is_blocked = TRUE THEN 'BLOCKED_BY_ADMIN'
                       ELSE 'ACTIVE'
    END;

-- 기존 is_blocked 컬럼 삭제
ALTER TABLE cheer_talks
    DROP COLUMN is_blocked;