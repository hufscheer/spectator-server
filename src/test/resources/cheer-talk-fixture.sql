SET foreign_key_checks = 0;

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', true,
        '2024-06-15 10:00:00'),
       (2, 1, 'jane@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', false,
        '2024-06-15 09:30:00');

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, '8강', '8강');

-- 축구 응원톡 픽스처 (game_id=2) (game_id=1로 조회할 때 나오면 안되는 데이터)
INSERT
INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
            state, round, is_pk_taken)
VALUES (1, 1, 1, '축구 대전', '2023-11-10T10:00:00', 'abc321', '2023-11-15T10:15:00', '전반전', 'SCHEDULED', '8강', false);

INSERT
INTO game_teams (game_id, team_id, cheer_count, score, pk_score)
VALUES (1, 4, 1, 0, 0),
       (2, 3, 1, 0, 0),
       (3, 2, 1, 0, 0),
       (4, 1, 1, 0, 0);

INSERT INTO `cheer_talks` (id, `created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES (1, '2023-01-01 12:30:00', '응원톡1', false, 1), -- 신고된 응원톡
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

INSERT INTO reports(id, cheer_talk_id, reported_at, state)
VALUES (1, 1, '2023-01-01 12:30:00', 'PENDING'),
       (2, 18, '2023-01-01 12:30:00', 'PENDING'),
       (3, 14, '2023-01-01 12:30:00', 'VALID');
SET foreign_key_checks = 1;
