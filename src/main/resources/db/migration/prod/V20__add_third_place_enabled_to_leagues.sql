-- 3·4위전 진행 여부. 기존 대회는 전부 미진행이며, 경기가 등록된 뒤에는 변경할 수 없다.
ALTER TABLE leagues
    ADD COLUMN third_place_enabled BOOLEAN NOT NULL DEFAULT FALSE;
