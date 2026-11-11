SET foreign_key_checks = 0;

-- 학교 1: 선수 소속, 학교 2: 다른 학교, 학교 3: 슈퍼유저와 다른 학교 대회 관리자의 소속
INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '학번 공개 학교', NULL),
       (2, '학번 공개 다른 학교', 10),
       (3, '학번 공개 제3 학교', 9);

-- id 1 은 슈퍼유저, 11 같은 학교 매니저, 12 다른 학교 매니저, 13 학교 3 소속이지만 학교 1 대회의 관리자
INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 3, 'super@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (11, 1, 'same-school@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (12, 2, 'other-school@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (13, 3, 'league-admin@example.com', 'password123', TRUE, '2024-07-01 10:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, '학번 공개 단과대', 1);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color, sport_type)
VALUES (1, 1, '학번 공개 팀', 'https://example.com/logos/sn.png', '#000000', 'SOCCER');

-- 학번 자리수가 다른 선수(9, 10자리), 앞자리가 20 이 아닌 학번, NULL, 4자리 미만, 4자리, 다른 학교 선수
INSERT INTO players (id, name, student_number, organization_id)
VALUES (1, '아홉자리', '202012345', 1),
       (2, '열자리', '2021123456', 1),
       (3, '앞자리삼삼', '331234567', 1),
       (4, '학번없음', NULL, 1),
       (5, '세자리', '123', 1),
       (6, '네자리', '2012', 1),
       (7, '다른학교선수', '202298765', 2);

INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 1, 1),
       (2, 1, 2, 2),
       (3, 1, 3, 3),
       (4, 1, 4, 4),
       (5, 1, 5, 5),
       (6, 1, 6, 6),
       (7, 1, 7, 7);

INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 13, 1, '학번 공개 대회', '2024-03-01 10:00:00', '2100-03-15 22:00:00', false, '8강', '8강', 'SOCCER');

INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1);

SET foreign_key_checks = 1;
