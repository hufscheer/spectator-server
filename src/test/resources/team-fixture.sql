SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '축구 협회');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'john@example.com', '$2a$10$yviVCR3GmaU6cPJT.8vaMOwph9WzbX6wtn9iERu3148ZP8XlKbakO', true,
        '2024-06-15 10:00:00');

-- 팀
INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'BUSINESS', '경영 야생마', 'https://example.com/logos/wildhorse.png', '#8B0000'),
       (2, 'BUSINESS', '서어 뻬데뻬', 'https://example.com/logos/pedro.png', '#FF4500'),
       (3, 'BUSINESS', '미컴 축구생각', 'https://example.com/logos/micom.png', '#1E90FF'),
       (4, 'BUSINESS', '체교 불사조', 'https://example.com/logos/phoenix.png', '#FFD700'),
       (5, 'BUSINESS', '컴공 독수리', 'https://example.com/logos/eagle.png', '#4B0082');

-- 선수
INSERT INTO players (id, name, student_number)
VALUES (1, '진승희', '202101001'),
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

SET foreign_key_checks = 1;