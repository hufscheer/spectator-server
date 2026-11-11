SET foreign_key_checks = 0;

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (2, 1, 1, '다른 대회', '2023-12-01 00:00:00', '2023-12-15 00:00:00', false, '4강', '4강');

INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter,
                   state, round)
VALUES (2, 1, 2, '다른 대회 경기', '2023-12-02T10:00:00', 'def456', '2023-12-02T10:15:00', 'PRE_GAME', 'SCHEDULED', '4강');

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score)
VALUES (2, 2, 1, 1, 0);

INSERT INTO cheer_talks (id, created_at, content, block_status, game_team_id)
VALUES (5, '2023-12-02 00:00:00', '다른 대회 신고된 댓글', 'ACTIVE', 2);

INSERT INTO reports (id, cheer_talk_id, reported_at, state)
VALUES (3, 5, '2023-12-02 00:00:00', 'VALID');

SET foreign_key_checks = 1;
