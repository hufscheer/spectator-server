SET foreign_key_checks = 0;

-- 오가니제이션
INSERT INTO organizations (id, name)
VALUES (1, '훕치치');

-- 매니저
INSERT INTO members (id, organization_id, email, password, is_manager, last_login)
VALUES (1, 1, 'john.doe@example.com', 'password123', TRUE, '2024-07-01 10:00:00'),
       (2, 1, 'jane.smith@example.com', 'password456', FALSE, '2024-07-02 12:30:00'),
       (3, 1, 'alice.johnson@example.com', 'password789', TRUE, '2024-07-03 09:45:00'),
       (4, 1, 'bob.brown@example.com', 'password321', FALSE, '2024-07-04 14:20:00'),
       (5, 1, 'carol.white@example.com', 'password654', TRUE, '2024-07-05 16:10:00');

-- 리그
INSERT INTO leagues (id, administrator_id, organization_id, name, start_at, end_at, is_deleted, max_round, in_progress_round)
VALUES (1, 1, 1, '삼건물 대회', '2023-11-09 00:00:00', '2023-11-20 00:00:00', false, '16강', '8강'),
       (2, 1, 1, '농구대잔치', '2023-11-10 00:00:00', '2023-11-15 00:00:00', false, '8강', '결승'),
       (3, 1, 1, '롤 대회', '2023-11-10 00:00:00', '2023-11-20 00:00:00', false, '8강', '8강'),
       (4, 1, 1, '루미큐브 대회', '2023-11-01 00:00:00', '2023-11-05 00:00:00', true, '16강', '8강'),
       (5, 1, 1, '삼건물 대회', '2022-11-09 00:00:00', '2022-11-20 00:00:00', false, '16강', '8강'),
       (6, 1, 1, '농구대잔치', '2022-11-10 00:00:00', '2022-11-15 00:00:00', false, '8강', '결승'),
       (7, 1, 1, '롤 대회', '2022-11-10 00:00:00', '2022-11-20 00:00:00', false, '8강', '8강'),
       (8, 2, 1, '탁구 대회', '2024-01-15 00:00:00', '2024-01-20 00:00:00', false, '16강', '16강'),
       (9, 1, 1, '야구 대회', '2024-01-01 00:00:00', '2099-12-31 00:00:00', false, '16강', '16강'),
       (10, 1, 1, '축구 대회', '2099-12-30 00:00:00', '2099-12-31 00:00:00', false, '16강', '16강');

-- 리그의 스포츠
INSERT INTO league_sports (id, league_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 4);

-- TEAMS 테이블 - 실제 팀 데이터
INSERT INTO teams (id, organization_id, logo_image_url, name, team_color, unit)
VALUES (1, 1, 'https://example.com/logo1.png', '경영 야생마', '#FF0000', 'ENGLISH'),
       (2, 1, 'https://example.com/logo2.png', '서어 뻬데뻬', '#00FF00', 'ENGLISH'),
       (3, 1, 'https://example.com/logo3.png', '미컴 축구생각', '#0000FF', 'ENGLISH'),
       (4, 1, 'https://example.com/logo4.png', '새로운 팀', '#FFFF00', 'ENGLISH'),
       (5, 1, 'https://example.com/logo5.png', '새로운 팀 2', '#FF00FF', 'ENGLISH'),
       (6, 1, 'https://example.com/logo6.png', '팀3', '#00FFFF', 'ENGLISH'),
       (7, 1, 'https://example.com/logo7.png', '팀4', '#FFA500', 'ENGLISH');

-- LEAGUE_TEAMS 테이블 - 리그와 팀의 매핑
INSERT INTO league_teams (id, league_id, team_id, ranking, total_cheer_count, total_talk_count)
VALUES (1, 1, 1, 1, 100, 50),
       (2, 1, 2, 2, 80, 40),
       (3, 1, 3, 3, 60, 30),
       (4, 1, 4, 4, 40, 20),
       (5, 1, 5, 5, 20, 10),
       (6, 9, 6, 1, 150, 75),
       (7, 9, 7, 2, 120, 60);

-- PLAYERS 테이블 - 선수 데이터
INSERT INTO players (id, name, student_number)
VALUES (1, '봄동나물진승희', '20200001'),
       (2, '가을전어이동규', '20200002'),
       (3, '겨울붕어빵이현제', '20200003'),
       (4, '여름수박고병룡', '20200004'),
       (5, '승희', '20200005'),
       (6, '김선수', '20200006'),
       (7, '박선수', '20200007'),
       (8, '이선수', '20200008'),
       (9, '최선수', '20200009'),
       (10, '정선수', '20200010');

-- TEAM_PLAYERS 테이블 - 팀과 선수의 매핑
INSERT INTO team_players (id, team_id, player_id)
VALUES (1, 1, 1),
       (2, 1, 5),
       (3, 3, 1),
       (4, 3, 2),
       (5, 3, 3),
       (6, 3, 4),
       (7, 6, 6),
       (8, 6, 7),
       (9, 7, 8),
       (10, 7, 9),
       (11, 7, 10);

-- LEAGUE_TEAM_PLAYERS 테이블 - 리그팀과 선수의 매핑
INSERT INTO league_team_players (id, league_team_id, player_id, jersey_number)
VALUES (1, 3, 1, 0),   -- 미컴 축구생각 - 봄동나물진승희
       (2, 3, 2, 2),   -- 미컴 축구생각 - 가을전어이동규
       (3, 3, 3, 3),   -- 미컴 축구생각 - 겨울붕어빵이현제
       (4, 3, 4, 4),   -- 미컴 축구생각 - 여름수박고병룡
       (5, 1, 5, 10),  -- 경영 야생마 - 승희
       (6, 6, 6, 1),   -- 팀3 - 김선수
       (7, 6, 7, 2),   -- 팀3 - 박선수
       (8, 7, 8, 3),   -- 팀4 - 이선수
       (9, 7, 9, 4),   -- 팀4 - 최선수
       (10, 7, 10, 5); -- 팀4 - 정선수

-- 게임 데이터
INSERT INTO games (id, manager_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round)
VALUES (1, 1, 1, '농구 대전', '2023-11-12T10:00:00', 'abc123', '2023-11-12T10:15:00', '1st Quarter', 'PLAYING', '4강'),
       (2, 1, 1, '두번째로 빠른 경기', '2023-11-12T10:10:00', 'abc123', '2023-11-12T10:10:00', '1st Quarter', 'SCHEDULED', '4강'),
       (3, 1, 1, '세번째로 빠른 경기', '2023-11-12T11:00:00', 'abc123', '2023-11-12T11:15:00', '1st Quarter', 'PLAYING', '4강'),
       (4, 1, 2, '네번째로 빠른 경기', '2023-11-12T12:00:00', 'abc123', '2023-11-12T12:15:00', '1st Quarter', 'PLAYING', '4강'),
       (5, 1, 1, '예시 경기', '2023-11-12T12:00:00', 'abc123', '2023-11-12T12:15:00', '1st Quarter', 'FINISHED', '4강');

-- 게임팀 데이터
INSERT INTO game_teams (game_id, team_id, cheer_count, score, pk_score)
VALUES (1, 1, 1, 1, 0),  -- 경영 야생마
       (1, 2, 2, 2, 0),  -- 서어 뻬데뻬

       (2, 2, 1, 0, 0),  -- 서어 뻬데뻬
       (2, 3, 1, 0, 0),  -- 미컴 축구생각

       (3, 1, 1, 0, 0),  -- 경영 야생마
       (3, 3, 1, 0, 0),  -- 미컴 축구생각

       (4, 4, 1, 1, 0),  -- 새로운 팀
       (4, 5, 2, 2, 0),  -- 새로운 팀 2

       (5, 1, 1, 0, 0),  -- 경영 야생마
       (5, 5, 1, 0, 0);  -- 새로운 팀 2

-- LEAGUE_STATISTICS 테이블
INSERT INTO league_statistics (id, league_id, first_winner_team_id, second_winner_team_id, most_cheered_team_id, most_cheer_talks_team_id)
VALUES (1, 1, 1, 2, 2, 3),  -- 삼건물 대회: 1등 경영야생마, 2등 서어뻬데뻬, 최다응원 서어뻬데뻬, 최다응원댓글 미컴축구생각
       (2, 2, 4, 5, 4, 5),  -- 농구대잔치: 1등 새로운팀, 2등 새로운팀2, 최다응원 새로운팀, 최다응원댓글 새로운팀2
       (3, 9, 6, 7, 6, 7);  -- 야구 대회: 1등 팀3, 2등 팀4, 최다응원 팀3, 최다응원댓글 팀4

SET foreign_key_checks = 1;