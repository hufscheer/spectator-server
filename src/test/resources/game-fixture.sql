SET
    foreign_key_checks = 0;

-- 경기
INSERT INTO games (id, sport_id, manager_id, league_id, name, start_time, video_id, quarter_changed_at,
                   game_quarter, state, round)
VALUES (1, 1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'SCHEDULED', '4강'),
       (2, 1, 1, 1, '두번째로 빠른 경기', '2023-11-12T10:10:00', 'abc123', '2023-11-12T10:10:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (3, 1, 1, 1, '세번째로 빠른 경기', '2023-11-12T11:00:00', 'abc123', '2023-11-12T11:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (4, 1, 1, 1, '네번째로 빠른 경기', '2023-11-12T12:00:00', 'abc123', '2023-11-12T12:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (5, 1, 1, 1, '여섯번째로 빠른 경기', '2023-11-12T13:00:00', 'abc123', '2023-11-12T13:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (6, 1, 1, 1, '다섯번째로 빠른 경기', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (7, 1, 1, 1, '다섯번째로 빠른 경기 2', '2023-11-12T12:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (8, 1, 1, 1, '일곱번째로 빠른 경기', '2023-11-12T14:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (9, 1, 1, 1, '아홉번째로 빠른 경기', '2023-11-12T15:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '4강'),
       (10, 1, 1, 1, '열번째로 빠른 경기', '2023-11-12T16:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '8강'),
       (11, 1, 1, 1, '열한번째로 빠른 경기', '2023-11-12T17:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '8강'),
       (12, 2, 1, 1, '열두번째로 빠른 경기', '2023-11-12T18:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '8강'),
       (13, 2, 1, 1, '열세번째로 빠른 경기', '2023-11-12T19:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'SCHEDULED',
        '8강'),
       (14, 2, 1, 1, '열네번쨰로 빠른 경기', '2023-11-12T20:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED',
        '8강'),
       (15, 2, 1, 1, '열다섯번째로 빠른 경기', '2023-11-12T21:10:00', 'abc123', '2023-11-12T14:15:00', '1st Quarter', 'FINISHED',
        '8강'),
       (19, 1, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'SCHEDULED', '8강'),
       (18, 1, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'SCHEDULED', '8강'),
       (16, 1, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'SCHEDULED', '8강'),
       (17, 1, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'SCHEDULED', '8강'),
       (23, 1, 1, 1, '12월 중 첫번째 빠른 경기', '2023-12-03T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'FINISHED', '4강'),
       (22, 1, 1, 1, '12월 중 두번째 빠른 경기', '2023-12-04T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'FINISHED', '4강'),
       (20, 1, 1, 1, '12월 중 세번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'FINISHED', '4강'),
       (21, 1, 1, 1, '12월 중 네번째 빠른 경기', '2023-12-05T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter',
        'FINISHED', '4강'),
       (24, 1, 1, 1, '결승전', '2023-12-06T10:00:00', 'abc123', '2023-12-06T10:00:00', '1st Quarter', 'SCHEDULED', '결승');

-- 팀
INSERT
INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀 A', 'http://example.com/logo_a.png', 1, 1, 1);

INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀 B', 'http://example.com/logo_b.png', 1, 1, 1);

INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀 C', 'http://example.com/logo_c.png', 1, 1, 1);

INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀 D', 'http://example.com/logo_c.png', 1, 1, 1);

INSERT INTO league_teams (name, logo_image_url, manager_id, organization_id, league_id)
VALUES ('팀 E', 'http://example.com/logo_c.png', 3, 2, 2);

-- league_team_id가 1인 선수들
INSERT INTO league_team_players (league_team_id, name, description, number)
VALUES (1, '김철수', '능숙한 포워드', 10),
       (1, '이영희', '민첩한 미드필더', 8),
       (1, '박지훈', '강력한 수비수', 5),
       (1, '최수진', '빠른 윙어', 7),
       (1, '정민수', '신뢰할 수 있는 골키퍼', 1);

-- league_team_id가 2인 선수들
INSERT INTO league_team_players (league_team_id, name, description, number)
VALUES (2, '홍길동', '경험 많은 포워드', 11),
       (2, '김민아', '다재다능한 미드필더', 6),
       (2, '박성호', '탄탄한 수비수', 4),
       (2, '이하나', '빠른 윙어', 9),
       (2, '최준혁', '재능 있는 골키퍼', 2);

-- 스포츠
INSERT INTO sports(id, name)
VALUES (1, '농구'),
       (2, '루미큐브'),
       (3, '축구');

-- 농구 대전(game_id = 1) A팀 선수
INSERT INTO lineup_players (id, game_team_id, name, description, number, is_captain, league_team_player_id, state,
                            is_playing)
VALUES (1, 1, '선수1', '센터', 1, false, 1, 'CANDIDATE', false),
       (2, 1, '선수2', '파워 포워드', 2, false, 2, 'STARTER', false),
       (3, 1, '선수3', '슈팅 가드', 3, false, 3, 'STARTER', false),
       (4, 1, '선수4', '포인트 가드', 4, false, 4, 'STARTER', false),
       (5, 1, '선수5', '스몰 포워드', 5, false, 5, 'STARTER', false);


-- 농구 대전(game_id = 1) B팀 선수
INSERT INTO lineup_players (id, game_team_id, name, description, number, is_captain, league_team_player_id, state,
                            is_playing)
VALUES (6, 2, '선수6', '센터', 1, true, 1, 'CANDIDATE', false),
       (7, 2, '선수7', '파워 포워드', 2, false, 1, 'STARTER', false),
       (8, 2, '선수8', '슈팅 가드', 3, false, 1, 'STARTER', false),
       (9, 2, '선수9', '포인트 가드', 4, false, 1, 'STARTER', false),
       (10, 2, '선수10', '스몰 포워드', 5, false, 1, 'STARTER', false);

-- 농구 대전 (game_id = 1) A팀 vs B팀
-- User
INSERT INTO game_teams (game_id, league_team_id, cheer_count, score)
VALUES (1, 1, 1, 1),
       (1, 2, 2, 2),

       (2, 2, 1, 0),
       (2, 3, 1, 0),

       (3, 1, 1, 0),
       (3, 3, 1, 0),

       (4, 2, 1, 1),
       (4, 3, 2, 2),

       (5, 2, 1, 1),
       (5, 3, 2, 2),

       (6, 2, 1, 1),
       (6, 3, 2, 2),

       (7, 2, 1, 1),
       (7, 3, 2, 2),

       (8, 2, 1, 1),
       (8, 3, 2, 2),

       (9, 2, 1, 1),
       (9, 3, 2, 2),

       (10, 2, 1, 1),
       (10, 3, 2, 2),

       (11, 2, 1, 1),
       (11, 3, 2, 2),

       (12, 2, 1, 1),
       (12, 3, 2, 2),

       (13, 2, 1, 1),
       (13, 3, 2, 2),

       (14, 2, 1, 1),
       (14, 3, 2, 2),

       (15, 2, 1, 1),
       (15, 3, 2, 2),

       (24, 2, 10, 0), -- 결승 진출 팀 B
       (24, 4, 10, 0); -- 결승 진출 팀 D

INSERT
INTO leagues (id, manager_id, organization_id, name, start_at, end_at, max_round, in_progress_round, is_deleted)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', '16강', '16강', false);

INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 1, 'jane.smith@example.com', 'password456', FALSE, '2024-07-02 12:30:00'),
       (3, 1, 'alice.johnson@example.com', 'password789', TRUE, '2024-07-03 09:45:00'),
       (4, 1, 'bob.brown@example.com', 'password321', FALSE, '2024-07-04 14:20:00'),
       (5, 1, 'carol.white@example.com', 'password654', TRUE, '2024-07-05 16:10:00');

SET
    foreign_key_checks = 1;
