SET foreign_key_checks = 0;


INSERT INTO organizations (id, name)
VALUES (1, '외대 축구부'),
       (2, '총학생회');


INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 2, 'non.manager@example.com', 'password123', FALSE, '2024-07-01 10:00:00');


INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'SOCIAL_SCIENCES', '팀A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 'ETC', '팀B', 'http://example.com/logo_b.png', '#0000FF'),
       (3, 'ENGLISH', '팀C', 'http://example.com/logo_c.png', '#0000FF'),
       (4, 'SOCIAL_SCIENCES', '팀D', 'http://example.com/logo_d.png', '#0000FF');


INSERT INTO players (id, name, student_number)
VALUES (1, '선수1', '202100001'),
       (2, '선수2', '202100002'),
       (3, '선수3', '202300003'),
       (4, '선수4', '202100004'),
       (5, '선수5', '202100005'),
       (6, '가선수6', '202100006'),
       (7, '나선수7', '202100007'),
       (8, '다선수8', '202100008'),
       (9, '라선수9', '202100009'),
       (10, '마선수10', '202100010'),
       (11, '선수11', '202100011');


INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES
    -- 팀 A(1) 선수들
    (1, 1, 1, 1),
    (2, 1, 2, 2),
    (3, 1, 3, 3),
    (4, 1, 4, 4),
    (5, 1, 5, 5),
    -- 팀 B(2) 선수들
    (6, 2, 6, 6),
    (7, 2, 7, 7),
    (8, 2, 8, 8),
    (9, 2, 9, 9),
    (10, 2, 10, 10),
    -- 팀 C(3) 선수
    (11, 3, 11, 1);


INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '테스트 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강');


INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 10, 5, 1),
       (2, 1, 2, 8, 3, 2);


INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '외대 월드컵 4강', '2023-11-12 10:00:00', 'abc123', '2023-11-12 10:15:00', '후반전', 'PLAYING', '4강', FALSE),
       (2, 1, 1, '외대 월드컵 결승', '2023-11-13 14:00:00', 'def456', '2023-11-13 16:00:00', '후반전', 'FINISHED', '결승', FALSE);


INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 0, 4, 0, 'LOSE'), -- 팀 A
       (2, 1, 2, 0, 7, 1, 'WIN'), -- 팀 B
       (3, 2, 2, 0, 0, 0, 'LOSE'), -- 팀 B, 리그 준우승팀
       (4, 2, 3, 0, 1, 0, 'WIN'); -- 팀 C, 리그 우승팀


-- 팀A의 라인업 선수
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (1, 1, 1, 1, TRUE, 'STARTER', FALSE, null),
       (2, 1, 2, 2, FALSE, 'STARTER', FALSE, null),
       (3, 1, 3, 3, FALSE, 'STARTER', FALSE, null),
       (4, 1, 4, 4, FALSE, 'STARTER', FALSE, null),
       (5, 1, 5, 5, FALSE, 'CANDIDATE', FALSE, null);


-- 팀B의 라인업 선수
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (6, 2, 6, 6, TRUE, 'STARTER', FALSE, null),
       (7, 2, 7, 7, FALSE, 'STARTER', FALSE, null),
       (8, 2, 8, 8, FALSE, 'STARTER', FALSE, null),
       (9, 2, 9, 9, FALSE, 'STARTER', FALSE, null),
       (10, 2, 10, 10, FALSE, 'CANDIDATE', FALSE, null);


-- 타임라인 생성

-- 경기 1 시작 (PRE_GAME)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'PRE_GAME', 0, 'QUARTER_START', 1, 2, 0, 0, null, null);


-- 전반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'FIRST_HALF', 0, 'QUARTER_START', 1, 2, 0, 0, 'PRE_GAME', null);


INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES
    ('SCORE', 1, 'FIRST_HALF', 22, 2, 1, 1, 1, 2, 0), -- A팀 선수 2의 1득점 (전반전),  선수 2 전반 총 1골
    ('SCORE', 1, 'FIRST_HALF', 23, 3, 1, 1, 2, 2, 0), -- A팀 선수 3의 1득점 (전반전)
    ('SCORE', 1, 'FIRST_HALF', 24, 3, 1, 1, 3, 2, 0), -- A팀 선수 3의 1득점 (전반전)
    ('SCORE', 1, 'FIRST_HALF', 25, 3, 1, 1, 4, 2, 0); -- A팀 선수 3의 1득점 (전반전), 선수 3 전반 총 3골


-- B팀 6선수 OUT 7선수 IN (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('REPLACEMENT', 1, 'FIRST_HALF', 24, 6, 7);


-- 후반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'SECOND_HALF', 30, 'QUARTER_START', 1, 2, 2, 0, 'FIRST_HALF', '2023-11-12 10:05:00');


-- A팀 2선수 OUT 3선수 IN (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('REPLACEMENT', 1, 'SECOND_HALF', 10, 2, 3);


INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES
    ('SCORE', 1, 'SECOND_HALF', 13, 10, 1, 1, 4, 2, 1), -- B팀 선수 10의 1득점
    ('SCORE', 1, 'SECOND_HALF', 14, 10, 1, 1, 4, 2, 2), -- B팀 선수 10의 1득점
    ('SCORE', 1, 'SECOND_HALF', 15, 10, 1, 1, 4, 2, 3), -- B팀 선수 10의 1득점

    ('SCORE', 1, 'SECOND_HALF', 16, 7, 1, 1, 4, 2, 4), -- B팀 선수 7의 1득점
    ('SCORE', 1, 'SECOND_HALF', 17, 7, 1, 1, 4, 2, 5), -- B팀 선수 7의 1득점

    ('SCORE', 1, 'SECOND_HALF', 18, 9, 1, 1, 4, 2, 6), -- B팀 선수 9의 1득점
    ('SCORE', 1, 'SECOND_HALF', 19, 9, 1, 1, 4, 2, 7); -- B팀 선수 9의 1득점


-- 연장전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'EXTRA_TIME', 15, 'QUARTER_START', 1, 2, 2, 3, 'SECOND_HALF', '2023-11-12 10:10:00');


-- 승부차기 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'PENALTY_SHOOTOUT', 18, 'QUARTER_START', 1, 2, 2, 3, 'EXTRA_TIME', '2023-11-12 10:12:00');


-- PK 기록 (승부차기)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, is_success)
VALUES ('PK', 1, 'PENALTY_SHOOTOUT', 10, 10, TRUE); -- B팀 선수 10의 승부차기 득점


-- 경기 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'POST_GAME', 20, 'GAME_END', 1, 2, 2, 3, 'PENALTY_SHOOTOUT', '2023-11-12 10:15:00');


-- === 최근 경기 조회 위한 데이터 ===

INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (10, 'ETC', '게임없는팀', 'http://example.com/logo_no_game.png', '#000000'),
       (11, 'ETC', '게임2개팀', 'http://example.com/logo_few_game.png', '#00FF00'),
       (12, 'ETC', '게임많은팀', 'http://example.com/logo_many_game.png', '#FF00FF');

INSERT INTO players (id, name, student_number)
VALUES (20, '선수20', '202400001'),
       (21, '선수21', '202400002'),
       (22, '선수22', '202400003');

INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (20, 10, 20, 1),
       (21, 11, 21, 1),
       (22, 12, 22, 1);

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (10, 1, 1, '최근게임테스트리그', '2024-01-01 00:00:00', '2024-12-31 23:59:59', FALSE, '결승', '결승');

-- 리그 팀 등록
INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (10, 10, 10, 0, 0, 1),
       (11, 10, 11, 0, 0, 2),
       (12, 10, 12, 0, 0, 3);

-- 게임많은팀의 게임
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES 
    (100, 1, 10, '최근게임1', '2025-12-15 14:00:00', 'video100', '2024-12-15 16:00:00', '후반전', 'FINISHED', '결승', FALSE),
    (101, 1, 10, '최근게임2', '2025-12-10 14:00:00', 'video101', '2024-12-10 16:00:00', '후반전', 'FINISHED', '결승', FALSE),
    (102, 1, 10, '최근게임3', '2025-12-05 14:00:00', 'video102', '2024-12-05 16:00:00', '후반전', 'FINISHED', '결승', FALSE),
    (103, 1, 10, '최근게임4', '2025-11-30 14:00:00', 'video103', '2024-11-30 16:00:00', '후반전', 'FINISHED', '4강', FALSE),
    (104, 1, 10, '최근게임5', '2025-11-25 14:00:00', 'video104', '2024-11-25 16:00:00', '후반전', 'FINISHED', '4강', FALSE);

-- 게임2개팀의 게임
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES 
    (200, 1, 10, '게임2개팀 게임1', '2024-12-20 14:00:00', 'video200', '2024-12-20 16:00:00', '후반전', 'FINISHED', '결승', FALSE),
    (201, 1, 10, '게임2개팀 게임2', '2024-12-01 14:00:00', 'video201', '2024-12-01 16:00:00', '후반전', 'FINISHED', '결승', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES 
    (100, 100, 12, 0, 2, 0, 'WIN'),
    (101, 101, 12, 0, 1, 0, 'LOSE'),
    (102, 102, 12, 0, 3, 0, 'WIN'),
    (103, 103, 12, 0, 0, 0, 'LOSE'),
    (104, 104, 12, 0, 1, 0, 'WIN');

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES 
    (200, 200, 11, 0, 1, 0, 'WIN'),
    (201, 201, 11, 0, 0, 0, 'LOSE');

SET foreign_key_checks = 1;