SET foreign_key_checks = 0;

-- 스포츠
INSERT INTO sports (id, name) VALUES (1, '축구');
INSERT INTO sports (id, name) VALUES (2, '농구');
INSERT INTO sports (id, name) VALUES (3, '롤');
INSERT INTO sports (id, name) VALUES (4, '루미큐브');


-- 리그
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (4, 1, 1, '루미큐브 대회', '2023-11-01 00:00:00', '2023-11-05 00:00:00', true);

-- 리그의 스포츠
INSERT INTO league_sports (id, league_id, sport_id) VALUES (1, 1, 1);
INSERT INTO league_sports (id, league_id, sport_id) VALUES (1, 2, 2);
INSERT INTO league_sports (id, league_id, sport_id) VALUES (1, 3, 3);
INSERT INTO league_sports (id, league_id, sport_id) VALUES (1, 4, 4);

SET foreign_key_checks = 1;
