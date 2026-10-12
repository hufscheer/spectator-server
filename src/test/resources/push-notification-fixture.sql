SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '외대 축구부', 9);

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, '사회과학대학', 1);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (1, 1, '팀A', null, '#FF0000', 'SOCCER'),
       (2, 1, '팀B', null, '#0000FF', 'SOCCER'),
       (3, 1, '팀C', null, '#00FF00', 'BASKETBALL'),
       (4, 1, '팀D', null, '#00FFFF', 'BASKETBALL');

INSERT INTO players (id, name, student_number)
VALUES (1, '선수1', '202100001'),
       (2, '선수2', '202100002'),
       (3, '선수3', '202100003'),
       (4, '선수4', '202100004');

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '테스트 리그', '2023-11-01 00:00:00', '2033-11-30 23:59:59', FALSE, '결승', '4강', 'SOCCER'),
       (2, 1, 1, '농구 리그', '2023-11-01 00:00:00', '2033-11-30 23:59:59', FALSE, '결승', '4강', 'BASKETBALL');

-- 1: 시작 전 / 2: 후반전 진행 중(1:1) / 3: 전반전 종료 후 / 4: 농구 시작 전 / 5: 후반전 진행 중(2:1) / 6: 농구 1쿼터 진행 중
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '시작 전', '2023-11-12 10:00:00', null, null, 'PRE_GAME', 'SCHEDULED', '4강', FALSE),
       (2, 1, 1, '후반전', '2023-11-12 10:00:00', null, '2023-11-12 10:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (3, 1, 1, '전반전 종료', '2023-11-12 10:00:00', null, '2023-11-12 10:45:00', 'FIRST_HALF', 'PLAYING', '4강', FALSE),
       (4, 1, 2, '농구 시작 전', '2023-11-12 10:00:00', null, null, 'PRE_GAME', 'SCHEDULED', '4강', FALSE),
       (5, 1, 1, '후반전 2:1', '2023-11-12 10:00:00', null, '2023-11-12 10:45:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (6, 1, 2, '농구 진행 중', '2023-11-12 10:00:00', null, '2023-11-12 10:00:00', 'FIRST_QUARTER', 'PLAYING', '4강', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 0, 0, 0, null),
       (2, 1, 2, 0, 0, 0, null),
       (3, 2, 1, 0, 1, 0, null),
       (4, 2, 2, 0, 1, 0, null),
       (5, 3, 1, 0, 0, 0, null),
       (6, 3, 2, 0, 0, 0, null),
       (7, 4, 3, 0, 0, 0, null),
       (8, 4, 4, 0, 0, 0, null),
       (9, 5, 1, 0, 2, 0, null),
       (10, 5, 2, 0, 1, 0, null),
       (11, 6, 3, 0, 0, 0, null),
       (12, 6, 4, 0, 0, 0, null);

INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (1, 3, 1, 1, TRUE, 'STARTER', TRUE, null),
       (2, 4, 2, 2, TRUE, 'STARTER', TRUE, null),
       (3, 11, 3, 3, TRUE, 'STARTER', TRUE, null);

INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 2, 'SECOND_HALF', 45, 'QUARTER_START', 3, 4, 1, 1, 'FIRST_HALF', '2023-11-12 10:30:00'),
       ('GAME_PROGRESS', 3, 'FIRST_HALF', 0, 'QUARTER_START', 5, 6, 0, 0, 'PRE_GAME', null),
       ('GAME_PROGRESS', 3, 'FIRST_HALF', 45, 'QUARTER_END', 5, 6, 0, 0, 'FIRST_HALF', '2023-11-12 10:00:00'),
       ('GAME_PROGRESS', 5, 'SECOND_HALF', 45, 'QUARTER_START', 9, 10, 2, 1, 'FIRST_HALF', '2023-11-12 10:30:00');
