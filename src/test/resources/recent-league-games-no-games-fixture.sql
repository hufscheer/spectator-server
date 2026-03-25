SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '테스트 조직');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'test@test.com', 'password', TRUE, '2025-01-01 00:00:00');

INSERT INTO teams (id, unit, name, logo_image_url, team_color)
VALUES (1, 'BUSINESS', '팀 A', 'http://logo.com/a.png', '#FF0000'),
       (2, 'BUSINESS', '팀 B', 'http://logo.com/b.png', '#0000FF');

-- 진행 중인 리그이지만 경기가 없음
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '경기 없는 진행중 대회', '2000-01-01 00:00:00', '2100-01-01 00:00:00', false, '4강', '4강');

INSERT INTO league_teams (id, league_id, team_id)
VALUES (1, 1, 1),
       (2, 1, 2);

-- games 없음

SET foreign_key_checks = 1;