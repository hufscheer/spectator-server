SET foreign_key_checks = 0;

-- 스포츠
INSERT INTO sports (id, name)
VALUES (1, '축구');
INSERT INTO sports (id, name)
VALUES (2, '농구');
INSERT INTO sports (id, name)
VALUES (3, '롤');
INSERT INTO sports (id, name)
VALUES (4, '루미큐브');

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
INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, '16강', '8강');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false, '8강', '결승');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false, '8강', '8강');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (4, 1, 1, '루미큐브 대회', '2023-11-01 00:00:00', '2023-11-05 00:00:00', true, '16강', '8강');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (5, 1, 1, '삼건물 대회', '2022-11-09 00:00:00', '2022-11-20 00:00:00', false, '16강', '8강');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (6, 1, 1, '농구대잔치', '2022-11-10 00:00:00', '2022-11-15 00:00:00', false, '8강', '결승');

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (7, 1, 1, '롤 대회', '2022-11-10 00:00:00', '2022-11-20 00:00:00', false, '8강', '8강');

-- 리그의 스포츠
INSERT INTO league_sports (id, league_id, sport_id)
VALUES (1, 1, 1);
INSERT INTO league_sports (id, league_id, sport_id)
VALUES (2, 2, 2);
INSERT INTO league_sports (id, league_id, sport_id)
VALUES (3, 3, 3);
INSERT INTO league_sports (id, league_id, sport_id)
VALUES (4, 4, 4);

-- 리그의 리그팀
INSERT INTO league_teams (id, name, logo_image_url, manager_id, organization_id, league_id)
VALUES (1, '경영 야생마', '이미지이미지', 1, 1, 1);

INSERT INTO league_teams (id, name, logo_image_url, manager_id, organization_id, league_id)
VALUES (2, '서어 뻬데뻬', '이미지이미지', 1, 1, 1);

INSERT INTO league_teams (id, name, logo_image_url, manager_id, organization_id, league_id)
VALUES (3, '미컴 축구생각', '이미지이미지', 1, 1, 1);

-- 리그팀의 선수
INSERT INTO league_team_players (id, name, description, number, league_team_id)
VALUES (1, '봄동나물진승희', '설명설명설명', 0, 3),
       (2, '가을전어이동규', '설명설명설명', 2, 3),
       (3, '겨울붕어빵이현제', '설명설명설명', 3, 3),
       (4, '여름수박고병룡', '설명설명설명', 3, 3),
       (5, '승희', '설명', 10, 1);

SET foreign_key_checks = 1;
