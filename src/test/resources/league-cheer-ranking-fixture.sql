SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '외대 축구부', 9);

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, '사회과학대학', 1);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color)
VALUES (1, 1, '팀A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 1, '팀B', 'http://example.com/logo_b.png', '#0000FF'),
       (3, 1, '팀C', 'http://example.com/logo_c.png', '#00FF00'),
       (4, 1, '팀D', 'http://example.com/logo_d.png', '#FFFF00'),
       (5, 1, '팀E', 'http://example.com/logo_e.png', '#FF00FF');

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '응원왕 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강', 'SOCCER');

INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 0, 0, 0),
       (2, 1, 2, 0, 0, 0),
       (3, 1, 3, 0, 0, 0),
       (4, 1, 4, 0, 0, 0),
       (5, 1, 5, 0, 0, 0);

-- 네 경기 모두 후반전 진행 중. 경기 종료 타임라인은 테스트에서 등록한다
-- 경기1 팀A vs 팀B (4강), 경기2 팀C vs 팀D (4강), 경기3 팀A vs 팀C (결승), 경기4 팀D vs 팀E (8강)
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '경기1', '2023-11-12 10:00:00', null, '2023-11-12 10:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (2, 1, 1, '경기2', '2023-11-12 14:00:00', null, '2023-11-12 14:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (3, 1, 1, '경기3', '2023-11-12 18:00:00', null, '2023-11-12 18:45:00', 'SECOND_HALF', 'PLAYING', '결승', FALSE),
       (4, 1, 1, '경기4', '2023-11-12 20:00:00', null, '2023-11-12 20:45:00', 'SECOND_HALF', 'PLAYING', '8강', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 100, 2, 0, null),
       (2, 1, 2, 50, 1, 0, null),
       (3, 2, 3, 90, 1, 0, null),
       (4, 2, 4, 80, 0, 0, null),
       (5, 3, 1, 10, 1, 0, null),
       (6, 3, 3, 5, 0, 0, null),
       (7, 4, 4, 0, 1, 0, null),
       (8, 4, 5, 80, 0, 0, null);

INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'SECOND_HALF', 45, 'QUARTER_START', 1, 2, 0, 0, 'FIRST_HALF', '2023-11-12 10:30:00'),
       ('GAME_PROGRESS', 2, 'SECOND_HALF', 45, 'QUARTER_START', 3, 4, 0, 0, 'FIRST_HALF', '2023-11-12 14:30:00'),
       ('GAME_PROGRESS', 3, 'SECOND_HALF', 45, 'QUARTER_START', 5, 6, 0, 0, 'FIRST_HALF', '2023-11-12 18:30:00'),
       ('GAME_PROGRESS', 4, 'SECOND_HALF', 45, 'QUARTER_START', 7, 8, 0, 0, 'FIRST_HALF', '2023-11-12 20:30:00');

-- 응원톡: 경기1 팀A 3(활성)+1(차단), 팀B 1 / 경기2 팀C 2, 팀D 2 / 경기3(결승) 팀A 5
INSERT INTO cheer_talks (id, created_at, content, block_status, game_team_id)
VALUES (1, '2023-11-12 10:10:00', 'A1', 'ACTIVE', 1),
       (2, '2023-11-12 10:11:00', 'A2', 'ACTIVE', 1),
       (3, '2023-11-12 10:12:00', 'A3', 'ACTIVE', 1),
       (4, '2023-11-12 10:13:00', 'A4 차단', 'BLOCKED_BY_ADMIN', 1),
       (5, '2023-11-12 10:14:00', 'B1', 'ACTIVE', 2),
       (6, '2023-11-12 14:10:00', 'C1', 'ACTIVE', 3),
       (7, '2023-11-12 14:11:00', 'C2', 'ACTIVE', 3),
       (8, '2023-11-12 14:12:00', 'D1', 'ACTIVE', 4),
       (9, '2023-11-12 14:13:00', 'D2', 'ACTIVE', 4),
       (10, '2023-11-12 18:10:00', 'F1', 'ACTIVE', 5),
       (11, '2023-11-12 18:11:00', 'F2', 'ACTIVE', 5),
       (12, '2023-11-12 18:12:00', 'F3', 'ACTIVE', 5),
       (13, '2023-11-12 18:13:00', 'F4', 'ACTIVE', 5),
       (14, '2023-11-12 18:14:00', 'F5', 'ACTIVE', 5);

SET foreign_key_checks = 1;
