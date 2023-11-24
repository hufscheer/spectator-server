SET foreign_key_checks = 0;

-- 경기
INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 2, 1, '롤 챔피언스', '2023-11-13T14:30:00', 'def456', '2023-11-13T15:00:00', '2nd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 3, 2, '루미큐브 대회', '2023-11-14T18:45:00', 'ghi789', '2023-11-14T19:00:00', '3rd Quarter', 'SCHEDULED');

-- 농구 대전과 시간이 같은 경우
INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 3, 1, '루미큐브 대회', '2023-11-12T10:00:00', 'ghi789', '2023-11-12T10:15:00', '3rd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 2, 1, '롤 복제 1', '2023-11-13T14:30:00', 'def456', '2023-11-13T15:00:00', '2nd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 3, 1, '루미큐브 복제 1', '2023-11-12T10:00:00', 'ghi789', '2023-11-12T10:15:00', '3rd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '농구 대전 복제 1', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 2, 1, '롤 복제 1', '2023-11-13T14:30:00', 'def456', '2023-11-13T15:00:00', '2nd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 3, 1, '루미큐브 복제 1', '2023-11-12T10:00:00', 'ghi789', '2023-11-12T10:15:00', '3rd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '농구 대전 복제 2', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 2, 1, '롤 복제 2', '2023-11-13T14:30:00', 'def456', '2023-11-13T15:00:00', '2nd Quarter', 'SCHEDULED');


-- 팀
INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 A', 'http://example.com/logo_a.png', 1, 1, 1);

INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 B', 'http://example.com/logo_b.png', 2, 1, 1);

INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 C', 'http://example.com/logo_c.png', 3, 2, 2);

-- 스포츠
INSERT INTO sports(id, name)
VALUES (1, '농구');

-- 농구 대전(game_id = 1) A팀 선수
INSERT INTO game_team_players (id, game_team_id, name, description)
VALUES (1, 1, '선수1', '센터'),
       (2, 1, '선수2', '파워 포워드'),
       (3, 1, '선수3', '슈팅 가드'),
       (4, 1, '선수4', '포인트 가드'),
       (5, 1, '선수5', '스몰 포워드');


-- 농구 대전(game_id = 1) B팀 선수
INSERT INTO game_team_players (id, game_team_id, name, description)
VALUES (6, 2, '선수6', '센터'),
       (7, 2, '선수7', '파워 포워드'),
       (8, 2, '선수8', '슈팅 가드'),
       (9, 2, '선수9', '포인트 가드'),
       (10, 2, '선수10', '스몰 포워드');

-- 농구 대전 (game_id = 1) A팀 vs B팀
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (1, 1, 1, 1), -- 팀 A의 정보
       (1, 2, 2, 2);

-- 롤 챔피언스 (game_id = 2) B팀 vs C팀
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (2, 2, 1, 0),
       (2, 3, 1, 0);

-- 루미큐브 대회 (game_id = 3) A팀 vs C팀
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (3, 1, 1, 0),
       (3, 3, 1, 0);

INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (5, 2, 1, 1), -- 팀 A의 정보
       (5, 3, 2, 2);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false);

SET foreign_key_checks = 1;