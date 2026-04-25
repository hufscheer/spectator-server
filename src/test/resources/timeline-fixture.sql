SET foreign_key_checks = 0;

-- 조직 생성
INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '외대 축구부', 9),
       (2, '총학생회', 9);

-- 관리자 계정 생성
INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 2, 'non.manager@example.com', 'password123', FALSE, '2024-07-01 10:00:00');

-- 단과대 생성
INSERT INTO units (id, name, organization_id)
VALUES (1, '사회과학대학', 1),
       (2, '기타', 1),
       (3, '영어대학', 1);

-- 팀 생성
INSERT INTO teams (id, unit_id, name, logo_image_url, team_color)
VALUES (1, 1, '팀A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 2, '팀B', 'http://example.com/logo_b.png', '#0000FF'),
       (3, 3, '팀C', 'http://example.com/logo_c.png', '#0000FF'),
       (4, 1, '팀D', 'http://example.com/logo_d.png', '#0000FF');

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
       (10, 2, 10, 10),

       (11, 3, 1, 11), -- 팀 3 선수들 (선수1이 다른 팀에도 속함)
       (12, 3, 2, 12), -- 팀 3에 선수2도 속함
       (13, 4, 3, 13), -- 팀 4 선수들 (선수3이 다른 팀에도 속함)
       (14, 4, 4, 14); -- 팀 4에 선수4도 속함

-- 리그 생성
INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '테스트 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강', 'SOCCER'),
       (2, 1, 1, '다른 리그', '2023-12-01 00:00:00', '2023-12-31 23:59:59', FALSE, '결승', '준결승', 'SOCCER'),
       (3, 1, 1, '농구 리그', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, '결승', '4강', 'BASKETBALL');

-- 리그 팀 연결
INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 10, 5, 1),
       (2, 1, 2, 8, 3, 2),
       (3, 2, 3, 5, 2, 1),  -- 다른 리그에 팀C 참여
       (4, 2, 4, 3, 1, 2);  -- 다른 리그에 팀D 참여

-- 경기 생성
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '농구 대전', '2023-11-12 10:00:00', 'abc123', '2023-11-12 10:15:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE),
       (2, 1, 1, '농구 대전 2', '2023-11-12 14:00:00', 'def456', '2023-11-12 14:15:00', 'SECOND_HALF', 'FINISHED', '4강', FALSE),
       (3, 1, 2, '다른 리그 경기', '2023-12-10 10:00:00', 'ghi789', '2023-12-10 10:15:00', 'FIRST_HALF', 'FINISHED', '준결승', FALSE),
       (4, 1, 1, '경기_시작_전_테스트용', '2023-11-12 10:00:00', null, null, 'PRE_GAME', 'SCHEDULED', '4강', FALSE),
       (5, 1, 3, '농구 경기', '2023-11-12 10:00:00', null, '2023-11-12 10:15:00', 'FIRST_QUARTER', 'PLAYING', '4강', FALSE);

-- 경기 팀 연결
INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 1, 15, 0, null), -- 팀 A
       (2, 1, 2, 2, 10, 0, null), -- 팀 B
       (3, 2, 1, 5, 20, 0, 'WIN'), -- 팀 A (2번 경기)
       (4, 2, 2, 3, 15, 0, 'LOSE'), -- 팀 B (2번 경기)
       (5, 3, 3, 2, 12, 0, 'WIN'), -- 팀 C (다른 리그 경기)
       (6, 3, 4, 1, 8, 0, 'LOSE'),  -- 팀 D (다른 리그 경기)
       (7, 5, 1, 0, 0, 0, null), -- 팀 A (농구 경기)
       (8, 5, 2, 0, 0, 0, null); -- 팀 B (농구 경기)

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

-- 라인업 선수 (2번 경기 - 팀A)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (11, 3, 1, 1, TRUE, 'STARTER', FALSE, null),  -- 선수1이 2번 경기에서 뛰게 됨
       (12, 3, 2, 2, FALSE, 'STARTER', FALSE, null); -- 선수2가 2번 경기에서 뛰게 됨

-- 라인업 선수 (3번 경기 - 다른 리그, 팀C)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (13, 5, 1, 11, TRUE, 'STARTER', FALSE, null),  -- 선수1이 다른 리그에서도 뛰게 됨
       (14, 5, 2, 12, FALSE, 'STARTER', FALSE, null); -- 선수2가 다른 리그에서도 뛰게 됨

-- 라인업 선수 (3번 경기 - 다른 리그, 팀D)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (15, 6, 3, 13, TRUE, 'STARTER', FALSE, null),  -- 선수3이 다른 리그에서도 뛰게 됨
       (16, 6, 4, 14, FALSE, 'STARTER', FALSE, null); -- 선수4가 다른 리그에서도 뛰게 됨

-- 라인업 선수 (5번 경기 - 농구 경기, 팀A)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (17, 7, 1, 1, TRUE, 'STARTER', TRUE, null),
       (18, 7, 2, 2, FALSE, 'STARTER', TRUE, null),
       (21, 7, 3, 3, FALSE, 'CANDIDATE', FALSE, null); -- 교체 대기 선수 (농구_교체_타임라인 테스트용)

-- 라인업 선수 (5번 경기 - 농구 경기, 팀B)
INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (19, 8, 6, 6, TRUE, 'STARTER', TRUE, null),
       (20, 8, 7, 7, FALSE, 'STARTER', TRUE, null);

-- 타임라인 생성

-- 경기 시작 (PRE_GAME)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'PRE_GAME', 0, 'QUARTER_START', 1, 2, 0, 0, null, null);

-- 전반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'FIRST_HALF', 0, 'QUARTER_START', 1, 2, 0, 0, 'PRE_GAME', null);

-- A팀 선수 2의 1득점 (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'FIRST_HALF', 22, 2, 1, 1, 1, 2, 0);

-- A팀 선수 1의 1득점 (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'FIRST_HALF', 25, 1, 1, 1, 2, 2, 0);

-- B팀 6선수 OUT 7선수 IN (전반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('SOCCER_REPLACEMENT', 1, 'FIRST_HALF', 24, 6, 7);

-- 후반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'SECOND_HALF', 30, 'QUARTER_START', 1, 2, 2, 0, 'FIRST_HALF', '2023-11-12 10:05:00');

-- A팀 2선수 OUT 3선수 IN (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, origin_lineup_player_id, replaced_lineup_player_id)
VALUES ('SOCCER_REPLACEMENT', 1, 'SECOND_HALF', 10, 2, 3);

-- B팀 선수 10의 1득점 (후반전)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'SECOND_HALF', 13, 10, 1, 1, 2, 2, 1);

-- 2번 경기에서 선수1과 선수2 추가 득점 (리그1에서 각각 총 2골씩 만들기 위함)
-- A팀 선수 1의 1득점 (2번 경기)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 2, 'FIRST_HALF', 15, 1, 1, 3, 1, 4, 0);

-- A팀 선수 2의 1득점 (2번 경기)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 2, 'FIRST_HALF', 20, 2, 1, 3, 2, 4, 0);

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

-- 다른 리그 경기 (3번 경기) 득점 타임라인들
-- 선수1이 다른 리그에서 5골 득점 (이 골들은 리그1 득점왕 집계에 포함되면 안됨)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 3, 'FIRST_HALF', 10, 13, 1, 5, 1, 6, 0),
       ('SCORE', 3, 'FIRST_HALF', 15, 13, 1, 5, 2, 6, 0),
       ('SCORE', 3, 'FIRST_HALF', 20, 13, 1, 5, 3, 6, 0),
       ('SCORE', 3, 'FIRST_HALF', 25, 13, 1, 5, 4, 6, 0),
       ('SCORE', 3, 'FIRST_HALF', 30, 13, 1, 5, 5, 6, 0);

-- 선수2도 다른 리그에서 3골 득점 (이 골들도 리그1 득점왕 집계에 포함되면 안됨)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 3, 'SECOND_HALF', 5, 14, 1, 5, 6, 6, 0),
       ('SCORE', 3, 'SECOND_HALF', 10, 14, 1, 5, 7, 6, 0),
       ('SCORE', 3, 'SECOND_HALF', 15, 14, 1, 5, 8, 6, 0);

-- 농구 경기 (5번 경기) 타임라인
-- 1쿼터 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 5, 'FIRST_QUARTER', 0, 'QUARTER_START', 7, 8, 0, 0, null, null);

-- 팀A(게임팀7) 선수17 3점 득점 (1쿼터)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 5, 'FIRST_QUARTER', 5, 17, 3, 7, 0, 8, 0);

-- 팀B(게임팀8) 선수19 2점 득점 (1쿼터)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 5, 'FIRST_QUARTER', 8, 19, 2, 7, 3, 8, 0);

-- 1쿼터 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 5, 'FIRST_QUARTER', 10, 'QUARTER_END', 7, 8, 3, 2, 'FIRST_QUARTER', null);

-- game 6: 경기종료 시 자동 쿼터종료 테스트용 (soccer, SECOND_HALF QUARTER_START 상태)
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (6, 1, 1, '자동_쿼터종료_테스트용', '2023-11-12 10:00:00', null, '2023-11-12 10:15:00', 'SECOND_HALF', 'PLAYING', '4강', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (9, 6, 1, 0, 0, 0, null),
       (10, 6, 2, 0, 0, 0, null);

INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 6, 'SECOND_HALF', 0, 'QUARTER_START', 'FIRST_HALF', null);

-- 응원톡 생성
INSERT INTO cheer_talks (id, game_team_id, content, created_at, block_status)
VALUES (1, 1, '화이팅!', '2023-11-12 10:05:00', 'ACTIVE'),
       (2, 2, '우리팀 최고!', '2023-11-12 10:10:00', 'ACTIVE'),
       (3, 1, '좋은 경기!', '2023-11-12 10:15:00', 'ACTIVE');

-- 리그 통계
INSERT INTO league_statistics (id, league_id, first_winner_team_id, second_winner_team_id, most_cheered_team_id, most_cheer_talks_team_id)
VALUES (1, 1, 1, 2, 1, 2);

SET foreign_key_checks = 1;