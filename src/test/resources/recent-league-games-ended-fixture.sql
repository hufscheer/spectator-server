SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '테스트 조직');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'test@test.com', 'password', TRUE, '2025-01-01 00:00:00');

INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'BUSINESS', '팀 A', 'http://logo.com/a.png', '#FF0000'),
       (2, 'BUSINESS', '팀 B', 'http://logo.com/b.png', '#0000FF');

-- 진행 중인 리그는 없음
-- 리그 1, 2는 동일한 종료일(2025-12-31)로 가장 최근 종료
-- 리그 3은 더 이전에 종료
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '최근 종료 대회 A', '2025-10-01 00:00:00', '2025-12-31 00:00:00', false, '4강', '4강'),
       (2, 1, 1, '최근 종료 대회 B', '2025-11-01 00:00:00', '2025-12-31 00:00:00', false, '4강', '4강'),
       (3, 1, 1, '이전에 종료된 대회', '2024-01-01 00:00:00', '2024-06-30 00:00:00', false, '4강', '4강');

INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 2, 1),
       (4, 2, 2),
       (5, 3, 1);

INSERT INTO games (id, administrator_id, league_id, name, start_time, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '최근종료A 경기1', '2025-12-01 10:00:00', 'FIRST_HALF', 'FINISHED', '4강', false),
       (2, 1, 1, '최근종료A 경기2', '2025-12-15 10:00:00', 'FIRST_HALF', 'FINISHED', '결승', false),
       (3, 1, 2, '최근종료B 경기', '2025-12-05 10:00:00', 'FIRST_HALF', 'FINISHED', '결승', false),
       (4, 1, 3, '이전종료 경기', '2024-03-01 10:00:00', 'FIRST_HALF', 'FINISHED', '결승', false);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score)
VALUES (1, 1, 1, 0, 1, 0),
       (2, 1, 2, 0, 0, 0),
       (3, 2, 1, 0, 2, 0),
       (4, 2, 2, 0, 1, 0),
       (5, 3, 1, 0, 1, 0),
       (6, 3, 2, 0, 0, 0),
       (7, 4, 1, 0, 0, 0),
       (8, 4, 2, 0, 1, 0);

SET foreign_key_checks = 1;