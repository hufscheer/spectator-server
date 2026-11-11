SET foreign_key_checks = 0;

INSERT INTO units (id, name, organization_id)
VALUES (2, '다른 학교 단과대', 2);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (7, 2, '다른 학교 축구팀', 'https://example.com/logos/other.png', '#000000', 'SOCCER');

-- 회원 2(학교 1)가 관리하는 대회
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (100, 2, 1, '회원 2의 대회', '2100-03-01 10:00:00', '2100-03-15 22:00:00', false, '8강', '8강', 'SOCCER');

SET foreign_key_checks = 1;
