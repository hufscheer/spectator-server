SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '학교A', 9),
       (2, '학교B', 9);

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'a@test.com', 'password', TRUE, '2025-01-01 00:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, 'A단과대', 1),
       (2, 'B단과대', 2);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (1, 1, 'A축구팀', 'https://example.com/a.png', '#8B0000', 'SOCCER'),
       (2, 2, 'B농구팀', 'https://example.com/b.png', '#FF4500', 'BASKETBALL'),
       (3, 2, 'B축구팀', 'https://example.com/c.png', '#1E90FF', 'SOCCER');

INSERT INTO players (id, name, student_number, organization_id)
VALUES (1, 'A축구선수', '202101001', 1),
       (2, 'B농구선수', '202101002', 2),
       (3, 'B축구선수', '202101003', 2);

-- 학교A 축구 2025, 학교B 농구 2025(가장 최근), 학교B 축구 2024
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, 'A축구대회', '2025-03-01 10:00:00', '2025-03-15 22:00:00', false, '4강', '4강', 'SOCCER'),
       (2, 1, 2, 'B농구대회', '2025-05-01 10:00:00', '2025-05-15 22:00:00', false, '4강', '4강', 'BASKETBALL'),
       (3, 1, 2, 'B축구대회', '2024-04-01 10:00:00', '2024-04-15 22:00:00', false, '4강', '4강', 'SOCCER');

INSERT INTO league_statistics (league_id, first_winner_team_id, second_winner_team_id, most_cheered_team_id, most_cheer_talks_team_id)
VALUES (1, 1, 3, 1, 1),
       (2, 2, 3, 2, 2),
       (3, 3, 2, 3, 3);

INSERT INTO league_top_scorers (id, league_id, player_id, ranking, goal_count, team_id)
VALUES (1, 1, 1, 1, 5, 1),
       (2, 2, 2, 1, 9, 2),
       (3, 3, 3, 1, 4, 3);

SET foreign_key_checks = 1;
