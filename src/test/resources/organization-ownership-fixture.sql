SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '학교 A', 9),
       (2, '학교 B', 9),
       (3, '학교 C', 9);

-- 1: 슈퍼유저(학교 A), 2: 학교 A 매니저, 3: 학교 B 매니저,
-- 4: 학교 C 소속이지만 학교 B 대회를 관리, 5: 학교 C 소속이고 삭제된 학교 B 대회를 관리했음
INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'super@example.com', 'x', true, '2024-06-15 10:00:00'),
       (2, 1, 'a@example.com', 'x', true, '2024-06-15 10:00:00'),
       (3, 2, 'b@example.com', 'x', true, '2024-06-15 10:00:00'),
       (4, 3, 'c-admin-b@example.com', 'x', true, '2024-06-15 10:00:00'),
       (5, 3, 'c-deleted@example.com', 'x', true, '2024-06-15 10:00:00');

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round,
                     in_progress_round, sport_type)
VALUES (1, 4, 2, '학교 B 대회', '2100-03-01 10:00:00', '2100-03-15 22:00:00', false, '8강', '8강', 'SOCCER'),
       (2, 5, 2, '삭제된 학교 B 대회', '2100-03-01 10:00:00', '2100-03-15 22:00:00', true, '8강', '8강', 'SOCCER');

INSERT INTO units (id, name, organization_id)
VALUES (1, '학교 A 단과대', 1),
       (2, '학교 B 단과대', 2);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (1, 1, '학교 A 팀', 'https://example.com/logos/a.png', '#111111', 'SOCCER'),
       (2, 2, '학교 B 팀', 'https://example.com/logos/b.png', '#222222', 'SOCCER');

INSERT INTO players (id, name, student_number, organization_id)
VALUES (1, '학교 A 선수', '202101001', 1),
       (2, '학교 B 선수', '202201001', 2),
       (3, '학교 B 선수 둘', '202201002', 2),
       (4, '학교 A 선수 둘', '202101002', 1);

INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 1, 9),
       (2, 2, 2, 7);

SET foreign_key_checks = 1;
