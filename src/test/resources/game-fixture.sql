SET foreign_key_checks = 0;

-- 경기
INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (2, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (3, 2, 1, '롤 챔피언스', '2023-11-13T14:30:00', 'def456', '2023-11-13T15:00:00', '2nd Quarter', 'SCHEDULED');

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (4, 3, 2, '루미큐브 대회', '2023-11-14T18:45:00', 'ghi789', '2023-11-14T19:00:00', '3rd Quarter', 'SCHEDULED');

-- 팀
INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 A', 'http://example.com/logo_a.png', 1, 1, 1);

INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 B', 'http://example.com/logo_b.png', 2, 1, 1);

INSERT INTO teams (name, logo_image_url, administrator_id, organization_id, league_id)
VALUES ('팀 C', 'http://example.com/logo_c.png', 3, 2, 2);

-- 경기의 팀
-- 농구 대전 (game_id = 1)
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (1, 1, 1, 0), -- 팀 A의 정보
       (1, 2, 1, 0);
-- 팀 B의 정보

-- 롤 챔피언스 (game_id = 2)
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (2, 2, 1, 0),
       (2, 3, 1, 0);
-- 팀 C의 정보

-- 루미큐브 대회 (game_id = 3)
INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (3, 1, 1, 0),
       (3, 3, 1, 0);

SET foreign_key_checks = 1;