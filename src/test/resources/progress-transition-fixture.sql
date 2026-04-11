SET foreign_key_checks = 0;

INSERT INTO organizations (id, name)
VALUES (1, '테스트 조직');

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'manager@example.com', 'password', TRUE, '2024-01-01 00:00:00');

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '축구 리그',   '2024-01-01 00:00:00', '2024-12-31 23:59:59', FALSE, '결승', '4강', 'SOCCER'),
       (2, 1, 1, '농구 리그',   '2024-01-01 00:00:00', '2024-12-31 23:59:59', FALSE, '결승', '4강', 'BASKETBALL');

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

-- ===== 농구 게임 =====
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES
    (10, 1, 2, '농구_경기_시작_전',    '2024-01-01 10:00:00', null, null,                   'PRE_GAME',       'SCHEDULED', '4강', FALSE),
    (11, 1, 2, '농구_1Q_진행_중',      '2024-01-01 10:00:00', null, '2024-01-01 10:00:00', 'FIRST_QUARTER',  'PLAYING',   '4강', FALSE),
    (12, 1, 2, '농구_1Q_종료_후',      '2024-01-01 10:00:00', null, '2024-01-01 10:00:00', 'FIRST_QUARTER',  'PLAYING',   '4강', FALSE),
    (13, 1, 2, '농구_2Q_진행_중',      '2024-01-01 10:00:00', null, '2024-01-01 10:15:00', 'SECOND_QUARTER', 'PLAYING',   '4강', FALSE),
    (14, 1, 2, '농구_2Q_종료_후',      '2024-01-01 10:00:00', null, '2024-01-01 10:30:00', 'SECOND_QUARTER', 'PLAYING',   '4강', FALSE),
    (15, 1, 2, '농구_3Q_진행_중',      '2024-01-01 10:00:00', null, '2024-01-01 10:45:00', 'THIRD_QUARTER',  'PLAYING',   '4강', FALSE),
    (16, 1, 2, '농구_3Q_종료_후',      '2024-01-01 10:00:00', null, '2024-01-01 11:00:00', 'THIRD_QUARTER',  'PLAYING',   '4강', FALSE),
    (17, 1, 2, '농구_4Q_진행_중',      '2024-01-01 10:00:00', null, '2024-01-01 11:15:00', 'FOURTH_QUARTER', 'PLAYING',   '4강', FALSE),
    (18, 1, 2, '농구_4Q_종료_후',      '2024-01-01 10:00:00', null, '2024-01-01 11:30:00', 'FOURTH_QUARTER', 'PLAYING',   '4강', FALSE),
    (19, 1, 2, '농구_OT_진행_중',      '2024-01-01 10:00:00', null, '2024-01-01 11:35:00', 'OVERTIME',       'PLAYING',   '4강', FALSE),
    (20, 1, 2, '농구_OT_종료_후',      '2024-01-01 10:00:00', null, '2024-01-01 11:45:00', 'OVERTIME',       'PLAYING',   '4강', FALSE),
    (21, 1, 2, '농구_경기_종료',       '2024-01-01 10:00:00', null, '2024-01-01 12:00:00', 'POST_GAME',      'FINISHED',  '4강', FALSE);

-- Game 10: 타임라인 없음 (농구 경기 시작 전)

-- Game 11: 1Q 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 11, 'FIRST_QUARTER', 0, 'QUARTER_START', 'PRE_GAME', null);

-- Game 12: 1Q 시작 → 1Q 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 12, 'FIRST_QUARTER', 0,  'QUARTER_START', 'PRE_GAME',      null),
       ('GAME_PROGRESS', 12, 'FIRST_QUARTER', 10, 'QUARTER_END',   'FIRST_QUARTER', '2024-01-01 10:00:00');

-- Game 13: ... → 2Q 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 13, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',      null),
       ('GAME_PROGRESS', 13, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER', '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 13, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER', '2024-01-01 10:15:00');

-- Game 14: ... → 2Q 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 14, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 14, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 14, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 14, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00');

-- Game 15: ... → 3Q 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 15, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 15, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 15, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 15, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 15, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00');

-- Game 16: ... → 3Q 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 16, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 16, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 16, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 16, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 16, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 16, 'THIRD_QUARTER',  40, 'QUARTER_END',   'THIRD_QUARTER',  '2024-01-01 11:00:00');

-- Game 17: ... → 4Q 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 17, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 17, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 17, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 17, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 17, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 17, 'THIRD_QUARTER',  40, 'QUARTER_END',   'THIRD_QUARTER',  '2024-01-01 11:00:00'),
       ('GAME_PROGRESS', 17, 'FOURTH_QUARTER', 45, 'QUARTER_START', 'THIRD_QUARTER',  '2024-01-01 11:15:00');

-- Game 18: ... → 4Q 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 18, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 18, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 18, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 18, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 18, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 18, 'THIRD_QUARTER',  40, 'QUARTER_END',   'THIRD_QUARTER',  '2024-01-01 11:00:00'),
       ('GAME_PROGRESS', 18, 'FOURTH_QUARTER', 45, 'QUARTER_START', 'THIRD_QUARTER',  '2024-01-01 11:15:00'),
       ('GAME_PROGRESS', 18, 'FOURTH_QUARTER', 55, 'QUARTER_END',   'FOURTH_QUARTER', '2024-01-01 11:30:00');

-- Game 19: ... → OT 시작
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 19, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 19, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 19, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 19, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 19, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 19, 'THIRD_QUARTER',  40, 'QUARTER_END',   'THIRD_QUARTER',  '2024-01-01 11:00:00'),
       ('GAME_PROGRESS', 19, 'FOURTH_QUARTER', 45, 'QUARTER_START', 'THIRD_QUARTER',  '2024-01-01 11:15:00'),
       ('GAME_PROGRESS', 19, 'FOURTH_QUARTER', 55, 'QUARTER_END',   'FOURTH_QUARTER', '2024-01-01 11:30:00'),
       ('GAME_PROGRESS', 19, 'OVERTIME',       60, 'QUARTER_START', 'FOURTH_QUARTER', '2024-01-01 11:35:00');

-- Game 20: ... → OT 종료
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 20, 'FIRST_QUARTER',  0,  'QUARTER_START', 'PRE_GAME',       null),
       ('GAME_PROGRESS', 20, 'FIRST_QUARTER',  10, 'QUARTER_END',   'FIRST_QUARTER',  '2024-01-01 10:00:00'),
       ('GAME_PROGRESS', 20, 'SECOND_QUARTER', 15, 'QUARTER_START', 'FIRST_QUARTER',  '2024-01-01 10:15:00'),
       ('GAME_PROGRESS', 20, 'SECOND_QUARTER', 25, 'QUARTER_END',   'SECOND_QUARTER', '2024-01-01 10:30:00'),
       ('GAME_PROGRESS', 20, 'THIRD_QUARTER',  30, 'QUARTER_START', 'SECOND_QUARTER', '2024-01-01 10:45:00'),
       ('GAME_PROGRESS', 20, 'THIRD_QUARTER',  40, 'QUARTER_END',   'THIRD_QUARTER',  '2024-01-01 11:00:00'),
       ('GAME_PROGRESS', 20, 'FOURTH_QUARTER', 45, 'QUARTER_START', 'THIRD_QUARTER',  '2024-01-01 11:15:00'),
       ('GAME_PROGRESS', 20, 'FOURTH_QUARTER', 55, 'QUARTER_END',   'FOURTH_QUARTER', '2024-01-01 11:30:00'),
       ('GAME_PROGRESS', 20, 'OVERTIME',       60, 'QUARTER_START', 'FOURTH_QUARTER', '2024-01-01 11:35:00'),
       ('GAME_PROGRESS', 20, 'OVERTIME',       65, 'QUARTER_END',   'OVERTIME',       '2024-01-01 11:45:00');

SET foreign_key_checks = 1;
