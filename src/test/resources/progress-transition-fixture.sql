SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '테스트 조직');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'manager@example.com', 'password', TRUE, '2024-01-01 00:00:00');

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '테스트 리그', '2024-01-01 00:00:00', '2024-12-31 23:59:59', FALSE, '결승', '4강');

-- 각 경기 진행 상태별 게임 (8개)
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES
    (1, 1, 1, '경기_시작_전',      '2024-01-01 10:00:00', null, null,                   'PRE_GAME',         'SCHEDULED', '4강', FALSE),
    (2, 1, 1, '전반전_진행_중',    '2024-01-01 10:00:00', null, '2024-01-01 10:00:00', 'FIRST_HALF',       'PLAYING',   '4강', FALSE),
    (3, 1, 1, '전반전_종료_후',    '2024-01-01 10:00:00', null, '2024-01-01 10:00:00', 'FIRST_HALF',       'PLAYING',   '4강', FALSE),
    (4, 1, 1, '후반전_진행_중',    '2024-01-01 10:00:00', null, '2024-01-01 10:45:00', 'SECOND_HALF',      'PLAYING',   '4강', FALSE),
    (5, 1, 1, '후반전_종료_후',    '2024-01-01 10:00:00', null, '2024-01-01 11:35:00', 'SECOND_HALF',      'PLAYING',   '4강', FALSE),
    (6, 1, 1, '연장전_진행_중',    '2024-01-01 10:00:00', null, '2024-01-01 11:50:00', 'EXTRA_TIME',       'PLAYING',   '4강', FALSE),
    (7, 1, 1, '연장전_종료_후',    '2024-01-01 10:00:00', null, '2024-01-01 12:05:00', 'EXTRA_TIME',       'PLAYING',   '4강', FALSE),
    (8, 1, 1, '승부차기_진행_중',  '2024-01-01 10:00:00', null, '2024-01-01 12:10:00', 'PENALTY_SHOOTOUT', 'PLAYING',   '4강', TRUE),
    (9, 1, 1, '경기_종료',        '2024-01-01 10:00:00', null, '2024-01-01 12:30:00', 'POST_GAME',        'FINISHED',  '4강', TRUE);

-- Game 1: 타임라인 없음 (경기 시작 전)

-- Game 2: 전반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 2, 'FIRST_HALF', 0, 'QUARTER_START', 'PRE_GAME', null);

-- Game 3: 전반전 시작 → 전반전 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 3, 'FIRST_HALF', 0,  'QUARTER_START', 'PRE_GAME',    null),
       ('GAME_PROGRESS', 3, 'FIRST_HALF', 45, 'QUARTER_END',   'FIRST_HALF', '2024-01-01 10:00:00');

-- Game 4: 전반전 시작 → 전반전 종료 → 후반전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 4, 'FIRST_HALF',  0,  'QUARTER_START', 'PRE_GAME',    null),
       ('GAME_PROGRESS', 4, 'FIRST_HALF',  45, 'QUARTER_END',   'FIRST_HALF', '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 4, 'SECOND_HALF', 50, 'QUARTER_START', 'FIRST_HALF', '2024-01-01 10:45:00');

-- Game 5: ... → 후반전 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 5, 'FIRST_HALF',  0,  'QUARTER_START', 'PRE_GAME',    null),
       ('GAME_PROGRESS', 5, 'FIRST_HALF',  45, 'QUARTER_END',   'FIRST_HALF', '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 5, 'SECOND_HALF', 50, 'QUARTER_START', 'FIRST_HALF', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 5, 'SECOND_HALF', 90, 'QUARTER_END',   'SECOND_HALF','2024-01-01 11:35:00');

-- Game 6: ... → 연장전 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 6, 'FIRST_HALF',  0,  'QUARTER_START', 'PRE_GAME',    null),
       ('GAME_PROGRESS', 6, 'FIRST_HALF',  45, 'QUARTER_END',   'FIRST_HALF', '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 6, 'SECOND_HALF', 50, 'QUARTER_START', 'FIRST_HALF', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 6, 'SECOND_HALF', 90, 'QUARTER_END',   'SECOND_HALF','2024-01-01 11:35:00'),
       ('GAME_PROGRESS', 6, 'EXTRA_TIME',  95, 'QUARTER_START', 'SECOND_HALF','2024-01-01 11:50:00');

-- Game 7: ... → 연장전 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 7, 'FIRST_HALF',  0,   'QUARTER_START', 'PRE_GAME',    null),
       ('GAME_PROGRESS', 7, 'FIRST_HALF',  45,  'QUARTER_END',   'FIRST_HALF', '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 7, 'SECOND_HALF', 50,  'QUARTER_START', 'FIRST_HALF', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 7, 'SECOND_HALF', 90,  'QUARTER_END',   'SECOND_HALF','2024-01-01 11:35:00'),
       ('GAME_PROGRESS', 7, 'EXTRA_TIME',  95,  'QUARTER_START', 'SECOND_HALF','2024-01-01 11:50:00'),
       ('GAME_PROGRESS', 7, 'EXTRA_TIME',  105, 'QUARTER_END',   'EXTRA_TIME', '2024-01-01 12:05:00');

-- Game 8: ... → 승부차기 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 8, 'FIRST_HALF',       0,   'QUARTER_START', 'PRE_GAME',         null),
       ('GAME_PROGRESS', 8, 'FIRST_HALF',       45,  'QUARTER_END',   'FIRST_HALF',       '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 8, 'SECOND_HALF',      50,  'QUARTER_START', 'FIRST_HALF',       '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 8, 'SECOND_HALF',      90,  'QUARTER_END',   'SECOND_HALF',      '2024-01-01 11:35:00'),
       ('GAME_PROGRESS', 8, 'EXTRA_TIME',       95,  'QUARTER_START', 'SECOND_HALF',      '2024-01-01 11:50:00'),
       ('GAME_PROGRESS', 8, 'EXTRA_TIME',       105, 'QUARTER_END',   'EXTRA_TIME',       '2024-01-01 12:05:00'),
       ('GAME_PROGRESS', 8, 'PENALTY_SHOOTOUT', 106, 'QUARTER_START', 'EXTRA_TIME',       '2024-01-01 12:10:00');

SET foreign_key_checks = 1;
