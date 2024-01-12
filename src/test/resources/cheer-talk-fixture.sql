SET foreign_key_checks = 0;


-- 농구 게임 (game_id = 1) 응원톡 픽스처
INSERT INTO games (id, sport_id, manager_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (1, 1, 1, 0), -- 팀 A의 정보
       (1, 2, 1, 0);

-- Comment 테이블에 데이터를 넣는 INSERT 쿼리
INSERT INTO `cheer_talks` (id, `created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES (1, '2023-01-01 12:30:00', '응원톡1', false, 1),
       (2, '2023-01-02 12:45:00', '응원톡2', false, 2),
       (3, '2023-01-02 12:45:05', '응원톡3', false, 2),
       (4, '2023-01-02 14:50:00', '응원톡4', false, 1),
       (5, '2023-01-02 14:55:00', '응원톡5', false, 1),
       (6, '2023-01-02 14:55:00', '응원톡6', false, 1),
       (7, '2023-01-02 15:00:00', '응원톡7', false, 2),
       (8, '2023-01-02 15:00:30', '응원톡8', false, 1),
       (9, '2023-01-02 15:02:00', '응원톡9', false, 2),
       (10, '2023-01-02 15:10:20', '응원톡10', false, 2),
       (11, '2023-01-02 15:15:30', '응원톡11', false, 2),
       (12, '2023-01-02 15:45:35', '응원톡12', false, 1),
       (13, '2023-01-02 16:00:00', '응원톡13', false, 1),
       (14, '2023-01-02 16:00:00', '블락된 응원톡', true, 1);


-- 축구 응원톡 픽스처 (game_id=2) (game_id=1로 조회할 때 나오면 안되는 데이터)
INSERT INTO games (sport_id, manager_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (1, 1, 1, '축구 대전', '2023-11-10T10:00:00', 'abc321', '2023-11-15T10:15:00', '전반전', 'SCHEDULED');

INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (2, 1, 1, 0),
       (2, 2, 1, 0);

INSERT INTO `cheer_talks` (id, `created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES (15, '2023-01-01 12:30:00', '응원톡15', false, 3),
       (16, '2023-01-01 12:30:00', '응원톡16', false, 4);

SET foreign_key_checks = 1;
