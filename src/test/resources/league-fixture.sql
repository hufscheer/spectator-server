SET foreign_key_checks = 0;

-- 오가니제이션
INSERT INTO organizations (id, name)
VALUES (1, '훕치치');

-- 매니저
INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 1, 'jane.smith@example.com', 'password456', FALSE, '2024-07-02 12:30:00'),
       (3, 1, 'alice.johnson@example.com', 'password789', TRUE, '2024-07-03 09:45:00'),
       (4, 1, 'bob.brown@example.com', 'password321', FALSE, '2024-07-04 14:20:00'),
       (5, 1, 'carol.white@example.com', 'password654', TRUE, '2024-07-05 16:10:00');

-- 리그
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, '16강', '8강'),
       (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false, '8강', '결승'),
       (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false, '8강', '8강'),
       (4, 1, 1, '루미큐브 대회', '2023-11-01 00:00:00', '2023-11-05 00:00:00', true, '16강', '8강'),
       (5, 1, 1, '삼건물 대회', '2022-11-09 00:00:00', '2022-11-20 00:00:00', false, '16강', '8강'),
       (6, 1, 1, '농구대잔치', '2022-11-10 00:00:00', '2022-11-15 00:00:00', false, '8강', '결승'),
       (7, 1, 1, '롤 대회', '2022-11-10 00:00:00', '2022-11-20 00:00:00', false, '8강', '8강'),
       (8, 2, 1, '탁구 대회', '2024-01-15 00:00:00', '2024-01-20 00:00:00', false, '16강', '16강'),
       (9, 1, 1, '야구 대회', '2024-01-01 00:00:00', '2099-12-31 00:00:00', false, '16강', '16강'),
       (10, 1, 1, '축구 대회', '2099-12-30 00:00:00', '2099-12-31 00:00:00', false, '16강', '16강');

-- 팀
INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'BUSINESS', '경영 야생마', 'https://example.com/logos/wildhorse.png', '#8B0000'),
       (2, 'BUSINESS', '서어 뻬데뻬', 'https://example.com/logos/pedro.png', '#FF4500'),
       (3, 'BUSINESS', '미컴 축구생각', 'https://example.com/logos/micom.png', '#1E90FF'),
       (4, 'BUSINESS', '체교 불사조', 'https://example.com/logos/phoenix.png', '#FFD700'),
       (5, 'BUSINESS', '컴공 독수리', 'https://example.com/logos/eagle.png', '#4B0082');

-- 선수
INSERT INTO players (id, name, student_number)
VALUES (1, '김승희', '202101001'),
       (2, '이동규', '202101002'),
       (3, '이현제', '202202001'),
       (4, '고병룡', '202202002'),
       (5, '박주장', '202003001');

-- 팀-선수 연결
INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 3, 9),
       (2, 1, 4, 11),
       (3, 3, 1, 10),
       (4, 3, 2, 7),
       (5, 4, 5, 1);

-- 리그
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '2025 훕치치 풋살 챔피언십', '2025-08-01T10:00:00', '2025-08-15T22:00:00', false, '8강', '8강'),
       (2, 1, 1, '2025 훕치치 농구대잔치', '2025-09-01T10:00:00', '2025-09-15T22:00:00', false, '4강', '4강');

-- 리그-팀 연결
INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 2, 4),
       (5, 2, 5);

-- 경기
INSERT INTO games (id, administrator_id, league_id, start_time, name, round, state)
VALUES (1, 1, 1, '2025-08-05T18:00:00', '8강 1경기', '8강', 'SCHEDULED'),
       (2, 1, 1, '2025-08-05T19:00:00', '8강 2경기', '8강', 'SCHEDULED');

-- 경기-팀
INSERT INTO game_teams (id, game_id, team_id, score)
VALUES (1, 1, 1, 0),
       (2, 1, 2, 0),
       (3, 2, 3, 0);

SET foreign_key_checks = 1;