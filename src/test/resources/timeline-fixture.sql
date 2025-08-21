SET foreign_key_checks = 0;

-- 조직 생성
INSERT INTO organizations (id, name)
VALUES (1, '외대 축구부'),
       (2, '총학생회');

-- 관리자 계정 생성
INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 2, 'non.manager@example.com', 'password123', FALSE, '2024-07-01 10:00:00');

-- 팀 생성
INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'SOCIAL_SCIENCES', '팀A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 'ETC', '팀B', 'http://example.com/logo_b.png', '#0000FF'),
       (3, 'ENGLISH', '팀C', 'http://example.com/logo_c.png', '#0000FF'),
       (4, 'SOCIAL_SCIENCES', '팀D', 'http://example.com/logo_d.png', '#0000FF');

-- 선수 생성
INSERT INTO players (id, name, student_number)
VALUES (1, '선수1', '202100001'),
       (2, '선수2', '202100002'),
       (3, '선수3', '202100003'),
       (4, '선수4', '202100004'),
       (5, '선수5', '202100005'),
       (6, '선수6', '202100006'),
       (7, '선수7', '202100007'),
       (8, '선수8', '202100008'),
       (9, '선수9', '202100009'),
       (10, '선수10', '202100010');

-- 팀 선수 연결
INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 1, 1), -- 팀 1 선수들
       (2, 1, 2, 2),
       (3, 1, 3, 3),
       (4, 1, 4, 4),
       (5, 1, 5, 5),

       (6, 2, 6, 6), -- 팀 2 선수들
       (7, 2, 7, 7),
       (8, 2, 8, 8),
       (9, 2, 9, 9),
       (10, 2, 10, 10);

-- 리그 생성
INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '테스트 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강');

-- 리그 팀 연결
INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 10, 5, 1),
       (2, 1, 2, 8, 3, 2);

-- 경기 생성
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '농구 대전', '2023-11-12 10:00:00', 'abc123', '2023-11-12 10:15:00', '후반전', 'PLAYING', '4강', FALSE),
       (2, 1, 1, '농구 대전 2', '2023-11-12 14:00:00', 'def456', '2023-11-12 14:15:00', '후반전', 'FINISHED', '4강', FALSE);

-- 경기 팀 연결
INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 1, 15, 0, null), -- 팀 A
       (2, 1, 2, 2, 10, 0, null), -- 팀 B
       (3, 2, 1, 5, 20, 0, 'WIN'), -- 팀 A (2번 경기)
       (4, 2, 2, 3, 15, 0, 'LOSE'); -- 팀 B (2번 경기)

-- 라인업 선수 (1번 경기 - 팀A)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (1, 1, 1, 1, TRUE, 'STARTER', FALSE, null),
       (2, 1, 2, 2, FALSE, 'STARTER', FALSE, null),
       (3, 1, 3, 3, FALSE, 'STARTER', FALSE, null),
       (4, 1, 4, 4, FALSE, 'STARTER', FALSE, null),
       (5, 1, 5, 5, FALSE, 'CANDIDATE', FALSE, null);

-- 라인업 선수 (1번 경기 - 팀B)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (6, 2, 6, 6, TRUE, 'STARTER', FALSE, null),
       (7, 2, 7, 7, FALSE, 'STARTER', FALSE, null),
       (8, 2, 8, 8, FALSE, 'STARTER', FALSE, null),
       (9, 2, 9, 9, FALSE, 'STARTER', FALSE, null),
       (10, 2, 10, 10, FALSE, 'CANDIDATE', FALSE, null);

-- 타임라인 생성

-- 경기 시작 (PRE_GAME)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'PRE_GAME', 0, 'QUARTER_START', 1, 2, 0, 0, null, null);

-- 전반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'FIRST_HALF', 0, 'QUARTER_START', 1, 2, 0, 0, 'PRE_GAME', null);

-- A팀 선수 2의 2득점 (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'FIRST_HALF', 22, 2, 2, 1, 2, 2, 0);

-- B팀 6선수 OUT 7선수 IN (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('REPLACEMENT', 1, 'FIRST_HALF', 24, 6, 7);

-- 후반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'SECOND_HALF', 30, 'QUARTER_START', 1, 2, 2, 0, 'FIRST_HALF', '2023-11-12 10:05:00');

-- A팀 2선수 OUT 3선수 IN (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('REPLACEMENT', 1, 'SECOND_HALF', 10, 2, 3);

-- B팀 선수 10의 3득점 (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'SECOND_HALF', 13, 10, 3, 1, 2, 2, 3);

-- 연장전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'EXTRA_TIME', 15, 'QUARTER_START', 1, 2, 2, 3, 'SECOND_HALF', '2023-11-12 10:10:00');

-- 승부차기 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'PENALTY_SHOOTOUT', 18, 'QUARTER_START', 1, 2, 2, 3, 'EXTRA_TIME', '2023-11-12 10:12:00');

-- PK 기록 (승부차기)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, is_success)
VALUES ('PK', 1, 'PENALTY_SHOOTOUT', 10, 10, TRUE);

-- 경기 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'POST_GAME', 20, 'GAME_END', 1, 2, 2, 3, 'PENALTY_SHOOTOUT', '2023-11-12 10:15:00');

-- 옐로우 카드 (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, warning_card_type)
VALUES ('WARNING_CARD', 1, 'SECOND_HALF', 25, 10, 'YELLOW');

-- 레드 카드 (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, warning_card_type)
VALUES ('WARNING_CARD', 1, 'FIRST_HALF', 25, 10, 'RED');

-- 응원톡 생성
INSERT INTO cheer_talks (id, game_team_id, content, created_at, is_blocked)
VALUES (1, 1, '화이팅!', '2023-11-12 10:05:00', FALSE),
       (2, 2, '우리팀 최고!', '2023-11-12 10:10:00', FALSE),
       (3, 1, '좋은 경기!', '2023-11-12 10:15:00', FALSE);

-- 리그 통계
INSERT INTO league_statistics (id, league_id, first_winner_team_id, second_winner_team_id, most_cheered_team_id, most_cheer_talks_team_id)
VALUES (1, 1, 1, 2, 1, 2);

SET foreign_key_checks = 1;