SET foreign_key_checks = 0;

INSERT INTO organizations (id, name, student_number_digits)
VALUES (1, '테스트 조직', 9);

INSERT INTO members (id, organization_id, email, password, is_administrator, last_login)
VALUES (1, 1, 'manager@example.com', 'password', TRUE, '2024-01-01 00:00:00');

INSERT INTO units (id, name, organization_id)
VALUES (1, '단과대', 1);

INSERT INTO teams (id, unit_id, name, logo_image_url, team_color)
VALUES (1, 1, '팀A', 'http://example.com/logo_a.png', '#FF0000'),
       (2, 1, '팀B', 'http://example.com/logo_b.png', '#0000FF');

INSERT INTO players (id, name, student_number)
VALUES (1, '선수1', '202100001'),
       (2, '선수2', '202100002');

INSERT INTO team_players (id, team_id, player_id, jersey_number)
VALUES (1, 1, 1, 1),
       (2, 2, 2, 2);

INSERT INTO leagues (id, organization_id, administrator_id, name, start_at, end_at, is_deleted, max_round, in_progress_round, sport_type)
VALUES (1, 1, 1, '테스트 리그', '2024-01-01 00:00:00', '2024-12-31 23:59:59', FALSE, '결승', '4강', 'SOCCER');

INSERT INTO league_teams (id, league_id, team_id, total_cheer_count, total_talk_count, ranking)
VALUES (1, 1, 1, 0, 0, 1),
       (2, 1, 2, 0, 0, 2);

-- 전반전이 진행 중인(1쿼터 종료까지 마친) 축구 경기
INSERT INTO games (id, administrator_id, league_id, name, start_time, video_id, quarter_changed_at, game_quarter, state, round, is_pk_taken)
VALUES (1, 1, 1, '자책골_쿼터_득점_테스트용', '2024-01-01 10:00:00', null, '2024-01-01 10:45:00', 'FIRST_HALF', 'PLAYING', '4강', FALSE);

INSERT INTO game_teams (id, game_id, team_id, cheer_count, score, pk_score, result)
VALUES (1, 1, 1, 0, 1, 0, null), -- 팀A: 정상 득점 1골
       (2, 1, 2, 0, 1, 0, null); -- 팀B: 팀A의 자책골로 1점 획득

INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing, replaced_player_id)
VALUES (1, 1, 1, 1, TRUE, 'STARTER', TRUE, null), -- 팀A 선수1
       (2, 2, 2, 2, TRUE, 'STARTER', TRUE, null); -- 팀B 선수2

INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'FIRST_HALF', 0, 'QUARTER_START', 1, 2, 0, 0, 'PRE_GAME', null);

-- 팀A 선수1의 정상 득점
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('SCORE', 1, 'FIRST_HALF', 10, 1, 1, 1, 1, 2, 0);

-- 팀A 선수1의 자책골 (팀B가 득점)
INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, scorer_id, score, game_team1_id, snapshot_score1, game_team2_id, snapshot_score2)
VALUES ('OWN_GOAL', 1, 'FIRST_HALF', 20, 1, 1, 1, 1, 2, 1);

INSERT INTO timelines (type, game_id, recorded_quarter, recorded_at, game_progress_type, game_team1_id, game_team2_id, snapshot_score1, snapshot_score2, previous_quarter, previous_quarter_changed_at)
VALUES ('GAME_PROGRESS', 1, 'FIRST_HALF', 45, 'QUARTER_END', 1, 2, 1, 1, 'FIRST_HALF', '2024-01-01 10:00:00');

SET foreign_key_checks = 1;
