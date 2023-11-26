SET foreign_key_checks = 0;

INSERT INTO games (id, sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at,
                   game_quarter,
                   state)
VALUES (1, 1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score)
VALUES (1, 1, 1, 1, 0),
       (2, 1, 2, 1, 0);

-- Comment 테이블에 데이터를 넣는 INSERT 쿼리
INSERT INTO `comments` (`created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES ('2023-01-01 12:30:00', 'Great play!', false, 1),
       ('2023-01-02 14:45:00', 'What a save!', false, 2);

SET foreign_key_checks = 1;