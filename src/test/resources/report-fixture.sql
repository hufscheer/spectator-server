SET foreign_key_checks = 0;

-- 신고할 댓글
INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (1, '2023-11-11 00:00:00', '아직 신고안된 댓글이야', false, 1);

INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (2, '2023-11-11 00:00:00', '이미 블락된 댓글이야', true, 1);

INSERT INTO cheer_talks (id, created_at, content, is_blocked, game_team_id)
VALUES (3, '2023-11-11 00:00:00', '이미 신고된 댓글이야', false, 1);

-- 신고
INSERT INTO reports (id, cheer_talk_id, reported_at, state)
VALUES (1, 3, '2023-11-11 00:00:00', 'UNCHECKED');

SET foreign_key_checks = 1;
