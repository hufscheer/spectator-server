SET
    foreign_key_checks = 0;

INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 2, 'non.manager@example.com', 'password123', FALSE, '2024-07-01 10:00:00');

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
INSERT INTO games (id, sport_id, manager_id, league_id, name, start_time, video_id, quarter_changed_at,
                   game_quarter, state, round)
VALUES (1, 1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '3쿼터', 'PLAYING', '4강');

-- 팀
INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀A', 'http://example.com/logo_a.png', 1, 1, 1),
       ('팀B', 'http://example.com/logo_b.png', 2, 1, 1);

-- 경기의 팀
-- 농구 대전 (game_id = 1) A팀 vs B팀
INSERT INTO game_teams (game_id, league_team_id, cheer_count, score, pk_score)
VALUES (1, 1, 1, 15, 0), -- 팀 A의 정보
       (1, 2, 2, 10, 0);
-- 팀 B의 정보

-- 농구 대전(game_id = 1) A팀(game_team_id = 1) 선수
INSERT INTO lineup_players (id, game_team_id, name, description, is_captain, number, league_team_player_id, is_playing)
VALUES (1, 1, '선수1', '센터', true, 1, 1, false),
       (2, 1, '선수2', '파워 포워드', false, 2, 1, false),
       (3, 1, '선수3', '슈팅 가드', false, 3, 1, false),
       (4, 1, '선수4', '포인트 가드', false, 4, 1, false),
       (5, 1, '선수5', '스몰 포워드', false, 5, 1, false);

-- 농구 대전(game_id = 1) B팀(game_team_id = 2) 선수
INSERT INTO lineup_players (id, game_team_id, name, description, is_captain, number, league_team_player_id, is_playing)
VALUES (6, 2, '선수6', '센터', true, 6, 1, false),
       (7, 2, '선수7', '파워 포워드', false, 7, 1, false),
       (8, 2, '선수8', '슈팅 가드', false, 8, 1, false),
       (9, 2, '선수9', '포인트 가드', false, 9, 1, false),
       (10, 2, '선수10', '스몰 포워드', false, 10, 1, false);

-- 1쿼터 경기 기록 추가

INSERT INTO timelines(type,
                      game_id,
                      recorded_quarter_id,
                      previous_quarter_id,
                      recorded_at,
                      game_progress_type)
VALUES ('GAME_PROGRESS', 1, 1, 1, 0, 'GAME_START');

-- A팀 선수 2의 2득점
INSERT INTO timelines (type,
                       game_id,
                       recorded_quarter_id,
                       recorded_at,
                       scorer_id,
                       score,
                       game_team1_id,
                       snapshot_score1,
                       game_team2_id,
                       snapshot_score2)
VALUES ('SCORE', -- type
        1, -- game_id
        1, -- recorded_quarter_id
        22, -- recorded_at (UNIX timestamp)
        2, -- scorer_id
        2, -- score
        1, -- game_team1_id
        2, -- snapshot_score1
        2, -- game_team2_id
        0 -- snapshot_score2
       );


-- B팀 6선수 OUT 7선수 IN
INSERT INTO timelines (type,
                       game_id,
                       recorded_quarter_id,
                       recorded_at,
                       origin_lineup_player_id,
                       replaced_lineup_player_id)
VALUES ('REPLACEMENT', -- type
        1, -- game_id
        1, -- recorded_quarter_id
        24, -- recorded_at (UNIX timestamp)
        6, -- origin_lineup_player_id
        7 -- replaced_lineup_player_id
       );

-- 2쿼터 경기 기록 추가

-- A팀 2선수 OUT 3선수 IN
INSERT INTO timelines (type,
                       game_id,
                       recorded_quarter_id,
                       recorded_at,
                       origin_lineup_player_id,
                       replaced_lineup_player_id)
VALUES ('REPLACEMENT', -- type
        1, -- game_id
        2, -- recorded_quarter_id
        10, -- recorded_at (UNIX timestamp)
        2, -- origin_lineup_player_id
        3 -- replaced_lineup_player_id
       );


-- B팀 선수 10의 3득점
INSERT INTO timelines (type,
                       game_id,
                       recorded_quarter_id,
                       recorded_at,
                       scorer_id,
                       score,
                       game_team1_id,
                       snapshot_score1,
                       game_team2_id,
                       snapshot_score2)
VALUES ('SCORE', -- type
        1, -- game_id
        2, -- recorded_quarter_id
        13, -- recorded_at (UNIX timestamp)
        10, -- scorer_id
        3, -- score
        1, -- game_team1_id
        2, -- snapshot_score1
        2, -- game_team2_id
        3 -- snapshot_score2
       );


INSERT INTO timelines(type,
                      game_id,
                      recorded_quarter_id,
                      previous_quarter_id,
                      recorded_at,
                      game_progress_type)
VALUES ('GAME_PROGRESS', 1, 2, 2, 20, 'GAME_END');

INSERT INTO timelines(type,
                      game_id,
                      recorded_quarter_id,
                      recorded_at,
                      scorer_id,
                      is_success)
VALUES ('PK', 1, 2, 10, 10, true);

SET
    foreign_key_checks = 1;
