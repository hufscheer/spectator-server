SET foreign_key_checks = 0;

-- 스포츠
INSERT INTO sports (id, name) VALUES (1, '농구');
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
VALUES ('팀 A', 'http://example.com/logo_a.png', 1, 1, 1),
       ('팀 B', 'http://example.com/logo_b.png', 2, 1, 1);

-- 경기의 팀
-- 농구 대전 (game_id = 1) A팀 vs B팀
INSERT INTO game_teams (game_id, league_team_id, cheer_count, score)
VALUES (1, 1, 1, 15), -- 팀 A의 정보
       (1, 2, 2, 10); -- 팀 B의 정보

-- 농구 대전(game_id = 1) A팀 선수
INSERT INTO lineup_players (id, game_team_id, name, description)
VALUES (1, 1, '선수1', '센터'),
       (2, 1, '선수2', '파워 포워드'),
       (3, 1, '선수3', '슈팅 가드'),
       (4, 1, '선수4', '포인트 가드'),
       (5, 1, '선수5', '스몰 포워드');

-- 농구 대전(game_id = 1) B팀 선수
INSERT INTO lineup_players (id, game_team_id, name, description)
VALUES (6, 2, '선수6', '센터'),
       (7, 2, '선수7', '파워 포워드'),
       (8, 2, '선수8', '슈팅 가드'),
       (9, 2, '선수9', '포인트 가드'),
       (10, 2, '선수10', '스몰 포워드');

-- 1쿼터 경기 기록 추가
INSERT INTO records (game_id, game_team_id, lineup_player_id, score, scored_quarter_id, scored_at)
VALUES (1, 1, 3, 2, 1, 3), -- 선수 3의 2득점
       (1, 2, 6, 2, 1, 10), -- 선수 6의 2득점
       (1, 1, 2, 3, 1, 14); -- 선수 2의 3득점
-- 2쿼터 경기 기록 추가
INSERT INTO records (game_id, game_team_id, lineup_player_id, score, scored_quarter_id, scored_at)
VALUES (1, 1, 1, 2, 2, 5), -- 선수 1의 2득점
       (1, 2, 10, 2, 2, 20), -- 선수 10의 2득점
       (1, 1, 5, 3, 2, 30); -- 선수 5의 3득점
-- 3쿼터 경기 기록 추가
INSERT INTO records (game_id, game_team_id, lineup_player_id, score, scored_quarter_id, scored_at)
VALUES (1, 1, 4, 3, 3, 1), -- 선수 4의 3득점
       (1, 2, 9, 2, 3, 25), -- 선수 9의 2득점
       (1, 1, 5, 3, 3, 39); -- 선수 5의 3득점

SET foreign_key_checks = 1;
