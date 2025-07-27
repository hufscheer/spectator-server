SET foreign_key_checks = 0;


INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', true,
        '2024-06-15 10:00:00'),
       (2, 1, 'jane@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', false,
        '2024-06-15 09:30:00');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, '8강', '8강');

INSERT INTO games (id, manager_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state, round)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '16강');

INSERT INTO game_teams (game_id, league_team_id, cheer_count, score)
VALUES (1, 1, 1, 0);

-- 신고할 댓글
INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (1, '2023-11-11 00:00:00', '아직 신고안된 댓글이야', false, 1);

INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (2, '2023-11-11 00:00:00', '이미 블락된 댓글이야', true, 1);

INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (3, '2023-11-11 00:00:00', '이미 신고된 댓글이야', false, 1),
       (4, '2023-11-11 00:00:00', '신고 확인 대기 중인 댓글이야', false, 1);

-- 신고
INSERT INTO reports (id, cheer_talk_id, reported_at, state)
VALUES (1, 3, '2023-11-11 00:00:00', 'UNCHECKED'),
       (2, 4, '2023-11-11 00:00:00', 'PENDING');

SET foreign_key_checks = 1;
