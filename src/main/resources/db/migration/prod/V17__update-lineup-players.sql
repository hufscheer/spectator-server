-- 1. is_playing 컬럼을 추가하고 기본값을 TRUE 로 설정
ALTER TABLE lineup_players
    ADD COLUMN is_playing BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. 기존 레코드의 is_playing 값을 TRUE 로 설정
UPDATE lineup_players
SET is_playing = TRUE;

-- 3 기본값을 FALSE 로 변경
ALTER TABLE lineup_players
    ALTER COLUMN is_playing SET DEFAULT FALSE;
