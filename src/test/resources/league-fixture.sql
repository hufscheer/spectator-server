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


-- 리그
INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, 16, 8);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false, 8, 2);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false, 8, 8);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (4, 1, 1, '루미큐브 대회', '2023-11-01 00:00:00', '2023-11-05 00:00:00', true);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (5, 1, 1, '삼건물 대회', '2022-11-09 00:00:00', '2022-11-20 00:00:00', false, 16, 8);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (6, 1, 1, '농구대잔치', '2022-11-10 00:00:00', '2022-11-15 00:00:00', false, 8, 2);

INSERT INTO leagues (id, manager_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (7, 1, 1, '롤 대회', '2022-11-10 00:00:00', '2022-11-20 00:00:00', false, 8, 8);

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

SET foreign_key_checks = 1;
