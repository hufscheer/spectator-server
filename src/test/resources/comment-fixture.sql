SET foreign_key_checks = 0;

INSERT INTO games (sport_id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state)
VALUES (2, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED');

INSERT INTO game_teams (game_id, team_id, cheer_count, score)
VALUES (1, 1, 1, 0), -- 팀 A의 정보
       (1, 2, 1, 0);

-- Comment 테이블에 데이터를 넣는 INSERT 쿼리
INSERT INTO `comments` (id, `created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES (1, '2023-01-01 12:30:00', '댓글1', false, 1),
       (2, '2023-01-02 12:45:00', '댓글2', false, 2),
       (3, '2023-01-02 12:45:05', '댓글3', false, 2),
       (4, '2023-01-02 14:50:00', '댓글4', false, 1),
       (5, '2023-01-02 14:55:00', '댓글5', false, 1),
       (6, '2023-01-02 14:55:00', '댓글6', false, 1),
       (7, '2023-01-02 15:00:00', '댓글7', false, 2),
       (8, '2023-01-02 15:00:30', '댓글8', false, 1),
       (9, '2023-01-02 15:02:00', '댓글9', false, 2),
       (10, '2023-01-02 15:10:20', '댓글10', false, 2),
       (11, '2023-01-02 15:15:30', '댓글11', false, 2),
       (12, '2023-01-02 15:45:35', '댓글12', false, 1),
       (13, '2023-01-02 16:00:00', '댓글13', false, 1),
       (14, '2023-01-02 16:00:00', '블락된 댓글', true, 1);

SET foreign_key_checks = 1;