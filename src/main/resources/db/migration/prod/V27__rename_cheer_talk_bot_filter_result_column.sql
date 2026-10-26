-- V3 가 엔티티(CheerTalkBotFilterHistory.cheerTalkBotFilterResult)와 다른 이름으로 컬럼을 만들어 필터링 이력 저장이 실패했다.
-- information_schema 로 분기하는 PREPARE 문은 로컬과 테스트의 H2 가 실행하지 못해 쓰지 않는다.
-- Flyway 로 만든 DB 는 모두 V3 의 이름을 갖고 있으므로 그대로 바꾼다.
ALTER TABLE cheer_talk_bot_filter_history RENAME COLUMN cheer_talk_filter_result TO cheer_talk_bot_filter_result;
