SET foreign_key_checks = 0;


INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '훕치치', 9);


INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2025-07-01 10:00:00'),
       (2, 1, 'user@example.com', 'password456', FALSE, '2025-07-02 12:30:00');


INSERT INTO units (id, name, organization_id)
VALUES (1, '경영대학', 1);


INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (1, 1, '경영 야생마', 'https://example.com/logos/1.png', '#8B0000', 'SOCCER'),
       (2, 1, '서어 뻬데뻬', 'https://example.com/logos/2.png', '#FF4500', 'SOCCER'),
       (3, 1, '미컴 축구생각', 'https://example.com/logos/3.png', '#1E90FF', 'SOCCER'),
       (4, 1, '체교 불사조', 'https://example.com/logos/4.png', '#FFD700', 'SOCCER'),
       (5, 1, '컴공 독수리', 'https://example.com/logos/5.png', '#4B0082', 'SOCCER'),
       (6, 1, '전자 번개', 'https://example.com/logos/6.png', '#FF6347', 'SOCCER'),
       (7, 1, '국제 호랑이', 'https://example.com/logos/7.png', '#2E8B57', 'SOCCER'),
       (8, 1, '사회 곰', 'https://example.com/logos/8.png', '#708090', 'SOCCER');


INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '대진표 없는 대회', '2025-08-01 10:00:00', '2025-08-15 22:00:00', false, '8강', '8강', 'SOCCER'),
       (2, 1, 1, '대진표 대회', '2025-08-01 10:00:00', '2025-08-15 22:00:00', false, '4강', '4강', 'SOCCER'),
       (3, 1, 1, '경기 연결된 대회', '2025-08-01 10:00:00', '2025-08-15 22:00:00', false, '4강', '4강', 'SOCCER');


INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 1, 4),
       (5, 1, 5),
       (6, 1, 6),
       (7, 2, 1),
       (8, 2, 2),
       (9, 2, 3),
       (10, 2, 4),
       (11, 3, 5),
       (12, 3, 6),
       (13, 3, 7),
       (14, 3, 8);


INSERT INTO games (id, administrator_id, league_id, start_time, name, round, state, game_quarter, is_pk_taken)
VALUES (1, 1, 2, '2025-08-05 18:00:00', '4강 1경기', '4강', 'SCHEDULED', '경기전', false),
       (2, 1, 3, '2025-08-05 18:00:00', '4강 1경기', '4강', 'FINISHED', '경기 종료', false),
       (3, 1, 2, '2025-08-05 19:00:00', '대진표와 다른 경기', '4강', 'SCHEDULED', '경기전', false),
       (4, 1, 2, '2025-08-10 18:00:00', '결승전', '결승', 'SCHEDULED', '경기전', false),
       (5, 1, 3, '2025-08-06 18:00:00', '재경기', '4강', 'SCHEDULED', '경기전', false);


INSERT INTO game_teams (id, game_id, team_id, score, pk_score, result, cheer_count)
VALUES (1, 1, 1, 0, 0, null, 0),
       (2, 1, 2, 0, 0, null, 0),
       (3, 2, 5, 2, 0, 'WIN', 0),
       (4, 2, 6, 1, 0, 'LOSE', 0),
       (5, 3, 1, 0, 0, null, 0),
       (6, 3, 3, 0, 0, null, 0),
       (7, 4, 1, 0, 0, null, 0),
       (8, 4, 3, 0, 0, null, 0),
       (9, 5, 5, 0, 0, null, 0),
       (10, 5, 6, 0, 0, null, 0);


INSERT INTO bracket_matches (id, league_id, round, match_number, team1_id, team2_id, game_id)
VALUES (1, 2, '4강', 1, 1, 2, null),
       (2, 2, '4강', 2, 3, 4, null),
       (3, 2, '결승', 1, null, null, null),
       (4, 3, '4강', 1, 5, 6, 2),
       (5, 3, '4강', 2, 7, 8, null),
       (6, 3, '결승', 1, null, null, null);


-- 리그 4: 3·4위전을 진행하는 대회. 준결승 두 경기가 끝나 패자(2번, 4번 팀)가 확정된 상태
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round,
                     in_progress_round, sport_type, third_place_match_enabled)
VALUES (4, 1, 1, '3·4위전 대회', '2025-08-01 10:00:00', '2025-08-15 22:00:00', false, '4강', '4강', 'SOCCER', true);

INSERT INTO league_teams (id, league_id, team_id)
VALUES (15, 4, 1),
       (16, 4, 2),
       (17, 4, 3),
       (18, 4, 4);

INSERT INTO games (id, administrator_id, league_id, start_time, name, round, state, game_quarter, is_pk_taken)
VALUES (6, 1, 4, '2025-08-05 18:00:00', '4강 1경기', '4강', 'FINISHED', '경기 종료', false),
       (7, 1, 4, '2025-08-05 20:00:00', '4강 2경기', '4강', 'FINISHED', '경기 종료', false);

INSERT INTO game_teams (id, game_id, team_id, score, pk_score, result, cheer_count)
VALUES (11, 6, 1, 2, 0, 'WIN', 0),
       (12, 6, 2, 0, 0, 'LOSE', 0),
       (13, 7, 3, 1, 0, 'WIN', 0),
       (14, 7, 4, 0, 0, 'LOSE', 0);

INSERT INTO bracket_matches (id, league_id, round, match_number, team1_id, team2_id, game_id)
VALUES (7, 4, '4강', 1, 1, 2, 6),
       (8, 4, '4강', 2, 3, 4, 7),
       (9, 4, '결승', 1, null, null, null),
       (10, 4, '3·4위전', 1, null, null, null);
