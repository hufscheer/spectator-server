SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '축구 협회', 9);

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', true,
        '2024-06-15 10:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, '경영대학', 1);

-- 1: 아무 데도 안 엮인 팀 / 2: 경기에만 편성된 팀 / 3: 대회에만 참가한 팀
INSERT INTO teams (id, unit_id, name, logo_image_url, team_color)
VALUES (1, 1, '엮이지 않은 팀', 'https://example.com/logos/free.png', '#8B0000'),
       (2, 1, '경기에 편성된 팀', 'https://example.com/logos/ingame.png', '#FF4500'),
       (3, 1, '대회에 참가한 팀', 'https://example.com/logos/inleague.png', '#1E90FF');

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round,
                     in_progress_round)
VALUES (1, 1, 1, '테스트 대회', '2025-11-01 00:00:00', '2025-12-31 00:00:00', false, '4강', '4강');

INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 3);

INSERT INTO games (id, administrator_id, league_id, name, start_time, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '테스트 경기', '2025-12-01 10:00:00', 'FIRST_HALF', 'FINISHED', '4강', false);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score)
VALUES (1, 1, 2, 0, 1, 0);

SET foreign_key_checks = 1;
