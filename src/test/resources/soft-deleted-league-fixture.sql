-- bracket-fixture.sql 뒤에 실행한다. 대진표와 경기가 연결된 리그 3 에 득점왕과 통계를 붙이고 리그를 soft delete 한다
SET foreign_key_checks = 0;

INSERT INTO players (id, name, student_number, organization_id)
VALUES (1, '선수1', '202100001', 1);

INSERT INTO league_top_scorers (id, league_id, player_id, ranking, goal_count)
VALUES (1, 3, 1, 1, 2);

INSERT INTO league_statistics (id, league_id)
VALUES (1, 3);

UPDATE leagues SET is_deleted = 1 WHERE id = 3;
