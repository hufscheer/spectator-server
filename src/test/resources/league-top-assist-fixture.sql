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
       (3, 1, '팀C', 'http://example.com/logo_c.png', '#00FF00');

INSERT INTO players (id, name, student_number)
VALUES (1, '선수1', '202100001'),
       (2, '선수2', '202100002'),
       (3, '선수3', '202100003'),
       (4, '선수4', '202100004'),
       (5, '선수5', '202100005'),
       (6, '선수6', '202100006');

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '도움왕 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강', 'SOCCER');

INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 0, 0, 1),
       (2, 1, 2, 0, 0, 2),
       (3, 1, 3, 0, 0, 3);

-- 세 경기 모두 후반전 진행 중 (결승이 아닌 4강). 경기 종료 타임라인은 테스트에서 등록한다
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '경기1 팀A vs 팀B', '2023-11-12 10:00:00', null, '2023-11-12 10:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (2, 1, 1, '경기2 팀A vs 팀C', '2023-11-12 14:00:00', null, '2023-11-12 14:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (3, 1, 1, '경기3 팀B vs 팀C', '2023-11-12 18:00:00', null, '2023-11-12 18:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 0, 3, 0, null),
       (2, 1, 2, 0, 1, 0, null),
       (3, 2, 1, 0, 1, 0, null),
       (4, 2, 3, 0, 1, 0, null),
       (5, 3, 2, 0, 0, 0, null),
       (6, 3, 3, 0, 1, 0, null);

-- 선수1은 팀A(경기1)와 팀C(경기2, 경기3)에서 뛴다
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (1, 1, 1, 1, TRUE, 'STARTER', TRUE, null),
       (2, 1, 4, 2, FALSE, 'STARTER', TRUE, null),
       (3, 2, 2, 3, TRUE, 'STARTER', TRUE, null),
       (4, 2, 3, 4, FALSE, 'STARTER', TRUE, null),
       (5, 3, 4, 1, TRUE, 'STARTER', TRUE, null),
       (6, 3, 6, 2, FALSE, 'STARTER', TRUE, null),
       (7, 4, 1, 3, TRUE, 'STARTER', TRUE, null),
       (8, 4, 5, 4, FALSE, 'STARTER', TRUE, null),
       (9, 5, 2, 1, TRUE, 'STARTER', TRUE, null),
       (10, 6, 1, 2, TRUE, 'STARTER', TRUE, null),
       (11, 6, 5, 3, FALSE, 'STARTER', TRUE, null);

-- 후반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'SECOND_HALF', 45, 'QUARTER_START', 1, 2, 0, 0, 'FIRST_HALF', '2023-11-12 10:30:00'),
       ('GAME_PROGRESS', 2, 'SECOND_HALF', 45, 'QUARTER_START', 3, 4, 0, 0, 'FIRST_HALF', '2023-11-12 14:30:00'),
       ('GAME_PROGRESS', 3, 'SECOND_HALF', 45, 'QUARTER_START', 5, 6, 0, 0, 'FIRST_HALF', '2023-11-12 18:30:00');

-- 경기1: 선수1이 팀A로 도움 2개(선수4의 골), 선수3이 팀B로 도움 1개(선수2의 골), 도움 없는 골 1개
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, assist_lineup_player_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'SECOND_HALF', 50, 2, 1, 1, 1, 1, 2, 0),
       ('SCORE', 1, 'SECOND_HALF', 60, 2, 1, 1, 1, 2, 2, 0),
       ('SCORE', 1, 'SECOND_HALF', 70, 3, 4, 1, 1, 2, 2, 1),
       ('SCORE', 1, 'SECOND_HALF', 80, 1, null, 1, 1, 3, 2, 1);

-- 경기2: 선수6이 팀A로 도움 1개, 선수1이 팀C로 도움 1개
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, assist_lineup_player_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 2, 'SECOND_HALF', 50, 5, 6, 1, 3, 1, 4, 0),
       ('SCORE', 2, 'SECOND_HALF', 60, 8, 7, 1, 3, 1, 4, 1);

-- 경기3: 선수1이 팀C로 도움 1개
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, assist_lineup_player_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 3, 'SECOND_HALF', 50, 11, 10, 1, 5, 0, 6, 1);

SET foreign_key_checks = 1;
