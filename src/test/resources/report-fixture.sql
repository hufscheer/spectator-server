SET foreign_key_checks = 0;

-- 신고할 댓글
INSERT INTO comments (id, created_at, content, is_blocked, game_team_id)
VALUES (1, '2023-11-11 00:00:00', '너무 못하네', false, 1);

INSERT INTO comments (id, created_at, content, is_blocked, game_team_id)
VALUES (2, '2023-11-11 00:00:00', '너무 못하네', true, 1);

SET foreign_key_checks = 1;
