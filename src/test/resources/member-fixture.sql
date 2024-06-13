SET foreign_key_checks = 0;

-- organization 테이블에 단체 이름 삽입
INSERT INTO organization (id, name)
VALUES (1, '축구 협회');
INSERT INTO organization (id, name)
VALUES (2, '농구 협회');
INSERT INTO organization (id, name)
VALUES (3, '리그 오브 레전드 동호회');
INSERT INTO organization (id, name)
VALUES (4, '루미큐브 클럽');

-- members 테이블에 데이터 삽입
INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john@example.com', 'hashed_password_1', true, '2024-06-15 10:00:00');
INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (2, 1, 'jane@example.com', 'hashed_password_2', false, '2024-06-15 09:30:00');
INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (3, 2, 'smith@example.com', 'hashed_password_3', false, '2024-06-14 17:45:00');

SET foreign_key_checks = 1;