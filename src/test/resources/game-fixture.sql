-- SET
--     foreign_key_checks = 0;
--
-- -- 경기
-- INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at,
--                    game_quarter, state, round, is_pk_taken)
-- VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '4강',
--         false),
--        (2,  1, 1, '두번째로 빠른 경기', '2023-11-12T10:10:00', 'abc123', '2023-11-12T10:10:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (3, 1, 1, '세번째로 빠른 경기', '2023-11-12T11:00:00', 'abc123', '2023-11-12T11:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (4, 1, 1, '네번째로 빠른 경기', '2023-11-12T12:00:00', 'abc123', '2023-11-12T12:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (5, 1, 1, '여섯번째로 빠른 경기', '2023-11-12T13:00:00', 'abc123', '2023-11-12T13:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (6, 1, 1, '다섯번째로 빠른 경기', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (7, 1, 1, '다섯번째로 빠른 경기 2', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (8, 1, 1, '일곱번째로 빠른 경기', '2023-11-12T14:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (9, 1, 1, '아홉번째로 빠른 경기', '2023-11-12T15:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '4강', false),
--        (10, 1, 1, '열번째로 빠른 경기', '2023-11-12T16:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '8강', false),
--        (11, 1, 1, '열한번째로 빠른 경기', '2023-11-12T17:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '8강', false),
--        (12, 1, 1, '열두번째로 빠른 경기', '2023-11-12T18:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '8강', false),
--        (13, 1, 1, '열세번째로 빠른 경기', '2023-11-12T19:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
--         '8강', false),
--        (14, 1, 1, '열네번쨰로 빠른 경기', '2023-11-12T20:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED',
--         '8강', false),
--        (15, 1, 1, '열다섯번째로 빠른 경기', '2023-11-12T21:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED',
--         '8강', false),
--        (19, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'SCHEDULED', '8강', false),
--        (18, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'SCHEDULED', '8강', false),
--        (16, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'SCHEDULED', '8강', false),
--        (17, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'SCHEDULED', '8강', false),
--        (23, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'FINISHED', '4강', false),
--        (22, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'FINISHED', '4강', false),
--        (20, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'FINISHED', '4강', false),
--        (21, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
--         'FINISHED', '4강', false),
--        (24, 1, 1, '결승전', '2023-12-06T10:00:00', 'abc123', '2023-12-06T10:00:00', '1st Quarter', 'SCHEDULED', '결승',
--         false);
--
-- -- 팀
-- INSERT INTO league_teams (name, logo_image_url, administrator_id, organization_id, league_id)
-- VALUES ('팀 A', 'http://example.com/logo_a.png', 1, 1, 1);
--
-- INSERT INTO league_teams (name, logo_image_url, administrator_id, organization_id, league_id)
-- VALUES ('팀 B', 'http://example.com/logo_b.png', 1, 1, 1);
--
-- INSERT INTO league_teams (name, logo_image_url, administrator_id, organization_id, league_id)
-- VALUES ('팀 C', 'http://example.com/logo_c.png', 1, 1, 1);
--
-- INSERT INTO league_teams (name, logo_image_url, administrator_id, organization_id, league_id)
-- VALUES ('팀 D', 'http://example.com/logo_c.png', 1, 1, 1);
--
-- INSERT INTO league_teams (name, logo_image_url, administrator_id, organization_id, league_id)
-- VALUES ('팀 E', 'http://example.com/logo_c.png', 3, 2, 2);
--
-- -- league_team_id가 1인 선수들
-- INSERT INTO league_team_players (league_team_id, name, description, number)
-- VALUES (1, '김철수', '능숙한 포워드', 10),
--        (1, '이영희', '민첩한 미드필더', 8),
--        (1, '박지훈', '강력한 수비수', 5),
--        (1, '최수진', '빠른 윙어', 7),
--        (1, '정민수', '신뢰할 수 있는 골키퍼', 1);
--
-- -- league_team_id가 2인 선수들
-- INSERT INTO league_team_players (league_team_id, name, description, number)
-- VALUES (2, '홍길동', '경험 많은 포워드', 11),
--        (2, '김민아', '다재다능한 미드필더', 6),
--        (2, '박성호', '탄탄한 수비수', 4),
--        (2, '이하나', '빠른 윙어', 9),
--        (2, '최준혁', '재능 있는 골키퍼', 2);
--
-- -- 쿼터
-- INSERT INTO quarters (id, name, _order)
-- VALUES (1, '1쿼터', 1),
--        (2, '2쿼터', 2),
--        (3, '3쿼터', 3),
--        (4, '4쿼터', 4);
--
-- -- 농구 대전(game_id = 1) A팀 선수
-- INSERT INTO lineup_players (id, game_team_id, name, description, number, is_captain, league_team_player_id, state,
--                             is_playing)
-- VALUES (1, 1, '선수1', '센터', 1, false, 1, 'CANDIDATE', false),
--        (2, 1, '선수2', '파워 포워드', 2, false, 2, 'STARTER', false),
--        (3, 1, '선수3', '슈팅 가드', 3, false, 3, 'STARTER', false),
--        (4, 1, '선수4', '포인트 가드', 4, false, 4, 'STARTER', false),
--        (5, 1, '선수5', '스몰 포워드', 5, false, 5, 'STARTER', true);
--
--
-- -- 농구 대전(game_id = 1) B팀 선수
-- INSERT INTO lineup_players (id, game_team_id, name, description, number, is_captain, league_team_player_id, state,
--                             is_playing)
-- VALUES (6, 2, '선수6', '센터', 1, true, 1, 'STARTER', false),
--        (7, 2, '선수7', '파워 포워드', 2, false, 1, 'STARTER', false),
--        (8, 2, '선수8', '슈팅 가드', 3, false, 1, 'STARTER', false),
--        (9, 2, '선수9', '포인트 가드', 4, false, 1, 'STARTER', false),
--        (10, 2, '선수10', '스몰 포워드', 5, false, 1, 'STARTER', true);
--
--
-- -- 농구 대전 (game_id = 1) A팀 vs B팀
-- -- User
-- INSERT INTO game_teams (game_id, league_team_id, cheer_count, score, pk_score)
-- VALUES (1, 1, 1, 1, 0),
--        (1, 2, 2, 2, 0),
--
--        (2, 2, 1, 0, 0),
--        (2, 3, 1, 0, 0),
--
--        (3, 1, 1, 0, 0),
--        (3, 3, 1, 0, 0),
--
--        (4, 2, 1, 1, 0),
--        (4, 3, 2, 2, 0),
--
--        (5, 2, 1, 1, 0),
--        (5, 3, 2, 2, 0),
--
--        (6, 2, 1, 1, 0),
--        (6, 3, 2, 2, 0),
--
--        (7, 2, 1, 1, 0),
--        (7, 3, 2, 2, 0),
--
--        (8, 2, 1, 1, 0),
--        (8, 3, 2, 2, 0),
--
--        (9, 2, 1, 1, 0),
--        (9, 3, 2, 2, 0),
--
--        (10, 2, 1, 1, 0),
--        (10, 3, 2, 2, 0),
--
--        (11, 2, 1, 1, 0),
--        (11, 3, 2, 2, 0),
--
--        (12, 2, 1, 1, 0),
--        (12, 3, 2, 2, 0),
--
--        (13, 2, 1, 1, 0),
--        (13, 3, 2, 2, 0),
--
--        (14, 2, 1, 1, 0),
--        (14, 3, 2, 2, 0),
--
--        (15, 2, 1, 1, 0),
--        (15, 3, 2, 2, 0),
--
--        (24, 2, 10, 0, 0), -- 결승 진출 팀 B
--        (24, 4, 10, 0, 0); -- 결승 진출 팀 D
--
-- INSERT
-- INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, max_round, in_progress_round, is_deleted)
-- VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', '16강', '16강', false);
--
-- INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
-- VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
--        (2, 1, 'jane.smith@example.com', 'password456', FALSE, '2024-07-02 12:30:00'),
--        (3, 1, 'alice.johnson@example.com', 'password789', TRUE, '2024-07-03 09:45:00'),
--        (4, 1, 'bob.brown@example.com', 'password321', FALSE, '2024-07-04 14:20:00'),
--        (5, 1, 'carol.white@example.com', 'password654', TRUE, '2024-07-05 16:10:00');
--
-- SET
--     foreign_key_checks = 1;

SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '외대 축구부'),
       (2, '총학생회');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', TRUE, '2024-07-01 10:00:00'),
       (2, 1, 'jane.smith@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', FALSE, '2024-07-02 12:30:00'),
       (3, 2, 'alice.johnson@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', TRUE, '2024-07-03 09:45:00'),
       (4, 1, 'bob.brown@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', FALSE, '2024-07-04 14:20:00'),
       (5, 1, 'carol.white@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', TRUE, '2024-07-05 16:10:00');

INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'ETC', '팀 A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 'ETC', '팀 B', 'http://example.com/logo_b.png', '#0000FF'),
       (3, 'ETC', '팀 C', 'http://example.com/logo_c.png', '#00FF00'),
       (4, 'ETC', '팀 D', 'http://example.com/logo_d.png', '#FFFF00'),
       (5, 'ETC', '팀 E', 'http://example.com/logo_e.png', '#800080'),
       (6, 'ETC', '팀 F', 'http://example.com/logo_e.png', '#FF0000'),
       (7, 'ETC', '팀 G', 'http://example.com/logo_e.png', '#00FF00');

INSERT INTO players (id, name, student_number)
VALUES (1, '김철수', '202401001'),
    (2, '이영희', '202401002'),
    (3, '박지훈', '202401003'),
    (4, '최수진', '202401004'),
    (5, '정민수', '202401005'),
    (6, '홍길동', '202401006'),
    (7, '김민아', '202401007'),
    (8, '박성호', '202401008'),
    (9, '이하나', '202401009'),
    (10, '최준혁', '202401010'),
    (11, '선수1', '202401011'),
    (12, '선수2', '202401012'),
    (13, '선수3', '202401013'),
    (14, '선수4', '202401014'),
    (15, '선수5', '202401015'),
    (16, '선수6', '202401016'),
    (17, '선수7', '202401017'),
    (18, '선수8', '202401018'),
    (19, '선수9', '202401019'),
    (20, '선수10', '202401020');

INSERT INTO team_players (team_id, player_id, jersey_number)
VALUES (1, 1, 10), -- 팀 A - 김철수
       (1, 2, 8),  -- 팀 A - 이영희
       (1, 3, 5),  -- 팀 A - 박지훈
       (1, 4, 7),  -- 팀 A - 최수진
       (1, 5, 1),  -- 팀 A - 정민수
       (2, 6, 11), -- 팀 B - 홍길동
       (2, 7, 6),  -- 팀 B - 김민아
       (2, 8, 4),  -- 팀 B - 박성호
       (2, 9, 9),  -- 팀 B - 이하나
       (2, 10, 2); -- 팀 B - 최준혁

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, max_round, in_progress_round, is_deleted)
VALUES (1, 1, 1, '삼건물 대회', '2024-11-09 00:00:00', '2025-11-20 00:00:00', '16강', '16강', false),
       (2, 3, 2, '외대 월드컵', '2024-12-01 00:00:00', '2025-12-31 00:00:00', '8강', '8강', false);

INSERT INTO league_teams (league_id, team_id)
VALUES (1, 6), -- 삼건물 대회 - 팀 A
       (1, 7), -- 삼건물 대회 - 팀 B
       (1, 3), -- 삼건물 대회 - 팀 C
       (1, 4), -- 삼건물 대회 - 팀 D
       (2, 5); -- 다른 리그 - 팀 E

INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at,
                   game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (2, 1, 1, '두번째로 빠른 경기', '2023-11-12T10:10:00', 'abc123', '2023-11-12T10:10:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (3, 1, 1, '세번째로 빠른 경기', '2023-11-12T11:00:00', 'abc123', '2023-11-12T11:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (4, 1, 1, '네번째로 빠른 경기', '2023-11-12T12:00:00', 'abc123', '2023-11-12T12:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (5, 1, 1, '여섯번째로 빠른 경기', '2023-11-12T13:00:00', 'abc123', '2023-11-12T13:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (6, 1, 1, '다섯번째로 빠른 경기', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (7, 1, 1, '다섯번째로 빠른 경기 2', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (8, 1, 1, '일곱번째로 빠른 경기', '2023-11-12T14:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (9, 1, 1, '아홉번째로 빠른 경기', '2023-11-12T15:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '4강', false),
       (10, 1, 1, '열번째로 빠른 경기', '2023-11-12T16:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (11, 1, 1, '열한번째로 빠른 경기', '2023-11-12T17:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (12, 1, 1, '열두번째로 빠른 경기', '2023-11-12T18:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (13, 1, 1, '열세번째로 빠른 경기', '2023-11-12T19:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (14, 1, 1, '열네번쨰로 빠른 경기', '2023-11-12T20:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED', '8강', false),
       (15, 1, 1, '열다섯번째로 빠른 경기', '2023-11-12T21:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED', '8강', false),
       (16, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (17, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (18, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (19, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '8강', false),
       (20, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'FINISHED', '4강', false),
       (21, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'FINISHED', '4강', false),
       (22, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'FINISHED', '4강', false),
       (23, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'FINISHED', '4강', false),
       (24, 1, 1, '결승전', '2023-12-06T10:00:00', 'abc123', '2023-12-06T10:00:00', '1st Quarter', 'SCHEDULED', '결승', false);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score)
VALUES (1, 1, 1, 1, 1, 0),   -- game 1, 팀 A
       (2, 1, 2, 2, 2, 0),   -- game 1, 팀 B
       (3, 2, 2, 1, 0, 0),   -- game 2, 팀 B
       (4, 2, 3, 1, 0, 0),   -- game 2, 팀 C
       (5, 3, 1, 1, 0, 0),   -- game 3, 팀 A
       (6, 3, 3, 1, 0, 0),   -- game 3, 팀 C
       (7, 4, 2, 1, 1, 0),
       (8, 4, 3, 2, 2, 0),
       (9, 5, 2, 1, 1, 0),
       (10, 5, 3, 2, 2, 0),
       (11, 6, 2, 1, 1, 0),
       (12, 6, 3, 2, 2, 0),
       (13, 7, 2, 1, 1, 0),
       (14, 7, 3, 2, 2, 0),
       (15, 8, 2, 1, 1, 0),
       (16, 8, 3, 2, 2, 0),
       (17, 9, 2, 1, 1, 0),
       (18, 9, 3, 2, 2, 0),
       (19, 10, 2, 1, 1, 0),
       (20, 10, 3, 2, 2, 0),
       (21, 11, 2, 1, 1, 0),
       (22, 11, 3, 2, 2, 0),
       (23, 12, 2, 1, 1, 0),
       (24, 12, 3, 2, 2, 0),
       (25, 13, 2, 1, 1, 0),
       (26, 13, 3, 2, 2, 0),
       (27, 14, 2, 1, 1, 0),
       (28, 14, 3, 2, 2, 0),
       (29, 15, 2, 1, 1, 0),
       (30, 15, 3, 2, 2, 0),
       (31, 24, 2, 10, 0, 0), -- 결승 진출 팀 B
       (32, 24, 4, 10, 0, 0); -- 결승 진출 팀 D

INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing)
VALUES -- 농구 대전(game_id = 1) A팀(game_team_id = 1) 선수
       (1, 1, 11, 1, false, 'CANDIDATE', false),
       (2, 1, 12, 2, false, 'STARTER', false),
       (3, 1, 13, 3, false, 'STARTER', false),
       (4, 1, 14, 4, false, 'STARTER', false),
       (5, 1, 15, 5, false, 'STARTER', true),
       -- 농구 대전(game_id = 1) B팀(game_team_id = 2) 선수
       (6, 2, 16, 1, true, 'STARTER', false),
       (7, 2, 17, 2, false, 'STARTER', false),
       (8, 2, 18, 3, false, 'STARTER', false),
       (9, 2, 19, 4, false, 'STARTER', false),
       (10, 2, 20, 5, false, 'STARTER', true);


SET foreign_key_checks = 1;