SET foreign_key_checks = 0;

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted)
VALUES (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false);

SET foreign_key_checks = 1;
