SET foreign_key_checks = 0;

INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00');

-- 스포츠
INSERT INTO sports (id, name)
VALUES (1, '농구');
-- 농구 쿼터
INSERT INTO quarters (id, name, sports_id)
VALUES (1, '1쿼터', 1),
       (2, '2쿼터', 1),
       (3, '3쿼터', 1),
       (4, '4쿼터', 1);

-- 경기
INSERT INTO games (sport_id, manager_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '3쿼터', 'PLAYING');

-- 팀
INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀A', 'http://example.com/logo_a.png', 1, 1, 1),
       ('팀B', 'http://example.com/logo_b.png', 2, 1, 1);

-- 경기의 팀
-- 농구 대전 (game_id = 1) A팀 vs B팀
INSERT INTO game_teams (game_id, league_team_id, cheer_count, score)
VALUES (1, 1, 1, 1), -- 팀 A의 정보
       (1, 2, 2, 2);
-- 팀 B의 정보

-- 농구 대전(game_id = 1) A팀(game_team_id = 1) 선수
INSERT INTO lineup_players (id, game_team_id, name, description, is_captain, number, league_team_player_id)
VALUES (1, 1, '선수1', '센터', true, 1, 1),
       (2, 1, '선수2', '파워 포워드', false, 2, 1),
       (3, 1, '선수3', '슈팅 가드', false, 3, 1),
       (4, 1, '선수4', '포인트 가드', false, 4, 1),
       (5, 1, '선수5', '스몰 포워드', false, 5, 1);

-- 농구 대전(game_id = 1) B팀(game_team_id = 2) 선수
INSERT INTO lineup_players (id, game_team_id, name, description, is_captain, number, league_team_player_id)
VALUES (6, 2, '선수6', '센터', true, 6, 1),
       (7, 2, '선수7', '파워 포워드', false, 7, 1),
       (8, 2, '선수8', '슈팅 가드', false, 8, 1),
       (9, 2, '선수9', '포인트 가드', false, 9, 1),
       (10, 2, '선수10', '스몰 포워드', false, 10, 1);

-- 1쿼터 경기 기록 추가
INSERT INTO records (id, game_id, game_team_id, recorded_quarter_id, recorded_at, record_type)
VALUES (1, 1, 1, 1, 2, 'SCORE');
INSERT INTO score_records (record_id, lineup_player_id, score)
VALUES (1, 2, 1); -- A팀 선수 2의 1득점

INSERT INTO records (id, game_id, game_team_id, recorded_quarter_id, recorded_at, record_type)
VALUES (2, 1, 2, 1, 4, 'REPLACEMENT');
INSERT INTO replacement_records(record_id, origin_lineup_player_id, replaced_lineup_player_id)
VALUES (2, 6, 7); -- B팀 6선수 OUT 7선수 IN

-- 2쿼터 경기 기록 추가
INSERT INTO records (id, game_id, game_team_id, recorded_quarter_id, recorded_at, record_type)
VALUES (3, 1, 1, 2, 10, 'REPLACEMENT');
INSERT INTO replacement_records(record_id, origin_lineup_player_id, replaced_lineup_player_id)
VALUES (3, 2, 3); -- A팀 2선수 OUT 3선수 IN

INSERT INTO records (id, game_id, game_team_id, recorded_quarter_id, recorded_at, record_type)
VALUES (4, 1, 2, 2, 13, 'SCORE');
INSERT INTO score_records (record_id, lineup_player_id, score)
VALUES (4, 10, 1); -- B팀 선수 10의 1득점

INSERT INTO records (id, game_id, game_team_id, recorded_quarter_id, recorded_at, record_type)
VALUES (5, 1, 2, 2, 15, 'SCORE');
INSERT INTO score_records (record_id, lineup_player_id, score)
VALUES (5, 10, 1); -- B팀 선수 10의 1득점

SET foreign_key_checks = 1;
