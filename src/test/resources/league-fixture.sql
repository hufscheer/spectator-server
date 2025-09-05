SET foreign_key_checks = 0;


INSERT INTO organizations (id, name)
VALUES (1, '훕치치'),
       (2, '외대 축구부');


INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2025-07-01 10:00:00'),
       (2, 1, 'user@example.com', 'password456', FALSE, '2025-07-02 12:30:00');


INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'BUSINESS', '경영 야생마', 'https://example.com/logos/wildhorse.png', '#8B0000'),
       (2, 'BUSINESS', '서어 뻬데뻬', 'https://example.com/logos/pedro.png', '#FF4500'),
       (3, 'BUSINESS', '미컴 축구생각', 'https://example.com/logos/micom.png', '#1E90FF'),
       (4, 'BUSINESS', '체교 불사조', 'https://example.com/logos/phoenix.png', '#FFD700'),
       (5, 'BUSINESS', '컴공 독수리', 'https://example.com/logos/eagle.png', '#4B0082');


INSERT INTO players (id, name, student_number)
VALUES (1, '진승희', '202101001'),
       (2, '이동규', '202101002'),
       (3, '이현제', '202202001'),
       (4, '고병룡', '202202002'),
       (5, '박주장', '202003001');


INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 3, 9),
       (2, 1, 4, 11),
       (3, 3, 1, 10),
       (4, 3, 2, 7),
       (5, 4, 5, 1);


INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES
    -- 종료된 경기들: id가 커질수록 최근 리그
    (1, 1, 1, '종료된 축구대회 1', '2024-03-01 10:00:00', '2024-03-15 22:00:00', false, '8강', '8강'),
    (2, 1, 1, '종료된 축구대회 2', '2025-02-01 10:00:00', '2025-02-15 22:00:00', false, '4강', '4강'),
    (3, 2, 1, '종료된 축구대회 3', '2025-03-01 00:00:00', '2025-03-15 00:00:00', false, '4강', '4강'),
    (4, 1, 1, '종료된 축구대회 4', '2025-04-10 10:00:00', '2025-04-25 22:00:00', false, '16강', '16강'),
    (5, 1, 1, '종료된 축구대회 5', '2025-05-01 10:00:00', '2025-05-15 22:00:00', false, '16강', '16강'),
    (6, 1, 1, '종료된 축구대회 6', '2025-06-01 10:00:00', '2025-06-15 22:00:00', false, '8강', '8강'),
    (7, 1, 1, '종료된 축구대회 7', '2025-07-01 10:00:00', '2025-07-15 22:00:00', false, '16강', '16강'),

    (8, 1, 1, '시작전 축구대회', '2100-08-01 10:00:00', '2100-08-15 22:00:00', false, '16강', '16강'),
    (9, 1, 1, '진행중인 축구대회', '2001-08-01 10:00:00', '2100-08-15 22:00:00', false, '16강', '16강'),
    (10, 1, 1, '삭제된 축구대회', '2001-08-01 10:00:00', '2100-08-15 22:00:00', true, '16강', '16강');


INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 2, 4),
       (5, 2, 5);


INSERT INTO games (id, administrator_id, league_id, start_time, name, round, state)
VALUES (1, 1, 1, '2025-08-05 18:00:00', '결승전', '결승', 'PLAYING'),
       (2, 1, 1, '2025-08-05 19:00:00', '8강 2경기', '8강', 'SCHEDULED');


INSERT INTO game_teams (id, game_id, team_id, score, result)
VALUES (1, 1, 1, 2, 'WIN'),
       (2, 1, 2, 1, 'LOSE'),
       (3, 2, 3, 0, null),
       (4, 2, 4, 0, null);


INSERT INTO league_statistics (league_id, first_winner_team_id, second_winner_team_id, most_cheered_team_id, most_cheer_talks_team_id)
VALUES (1, 1, 2, 1, 2),
       (2, 2, 1, 4, 3),
       (3, 3, 1, 1, 5),
       (4, 4, 3, 2, 2),
       (5, 5, 4, 5, 4),
       (6, 1, 3, 3, 1),
       (7, 2, 4, 4, 2);


INSERT INTO league_top_scorers (id, league_id, player_id, ranking, goal_count)
VALUES (1, 1, 1, 1, 5),
       (2, 1, 2, 2, 3),
       (3, 1, 3, 3, 2),
       (4, 2, 4, 1, 4),
       (5, 2, 5, 2, 2);


SET foreign_key_checks = 1;