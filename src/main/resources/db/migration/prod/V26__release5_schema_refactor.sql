-- V26: 5차 릴리즈 스키마 리팩토링

-- 사용하지 않는 도메인 테이블 삭제
DROP TABLE IF EXISTS league_sports;
DROP TABLE IF EXISTS sports;
DROP TABLE IF EXISTS quarters;

-- manager → administrator로 컬럼명 변경
ALTER TABLE members CHANGE is_manager is_administrator BOOLEAN NOT NULL;
ALTER TABLE leagues CHANGE manager_id administrator_id BIGINT NOT NULL;
ALTER TABLE games CHANGE manager_id administrator_id BIGINT NULL;

-- teams 테이블 신설
CREATE TABLE teams
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    unit           VARCHAR(255)          NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    logo_image_url VARCHAR(255)          NULL,
    team_color     VARCHAR(255)          NOT NULL,

    CONSTRAINT pk_teams PRIMARY KEY (id)
);

-- players 테이블 신설
CREATE TABLE players
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    student_number VARCHAR(255)          NULL,

    CONSTRAINT pk_players PRIMARY KEY (id),
    CONSTRAINT uc_players_student_number UNIQUE (student_number)
);

-- team_players 테이블 신설
CREATE TABLE team_players
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    team_id       BIGINT                NOT NULL,
    player_id     BIGINT                NOT NULL,
    jersey_number INTEGER               NULL,

    CONSTRAINT pk_team_players PRIMARY KEY (id),
    CONSTRAINT FK_TEAM_PLAYERS_ON_TEAMS FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT FK_TEAM_PLAYERS_ON_PLAYERS FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT uc_team_player UNIQUE (team_id, player_id)
);

-- game_teams 테이블 변경: league_team_id → team_id (새로운 teams 테이블 참조)
ALTER TABLE game_teams DROP FOREIGN KEY FK_GAME_TEAMS_ON_TEAM;
ALTER TABLE game_teams CHANGE league_team_id team_id BIGINT NOT NULL;
ALTER TABLE game_teams
    ADD CONSTRAINT FK_GAME_TEAMS_ON_TEAMS FOREIGN KEY (team_id) REFERENCES teams (id);
ALTER TABLE game_teams ADD COLUMN result VARCHAR(255) NULL;
ALTER TABLE game_teams
    ADD CONSTRAINT uc_game_team UNIQUE (game_id, team_id);

-- league_teams 테이블 변경: 리그-팀 매핑 테이블로 재정의, 통계 필드 추가
ALTER TABLE league_teams DROP FOREIGN KEY FK_TEAMS_ON_LEAGUE;
ALTER TABLE league_teams DROP FOREIGN KEY FK_TEAMS_ON_ADMINISTRATOR;
ALTER TABLE league_teams DROP FOREIGN KEY FK_TEAMS_ON_ORGANIZATION;

ALTER TABLE league_teams DROP COLUMN name;
ALTER TABLE league_teams DROP COLUMN logo_image_url;
ALTER TABLE league_teams DROP COLUMN manager_id;
ALTER TABLE league_teams DROP COLUMN organization_id;
ALTER TABLE league_teams DROP COLUMN team_color;

ALTER TABLE league_teams ADD COLUMN team_id BIGINT NOT NULL;
ALTER TABLE league_teams
    ADD CONSTRAINT FK_LEAGUE_TEAMS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id);
ALTER TABLE league_teams
    ADD CONSTRAINT FK_LEAGUE_TEAMS_ON_TEAMS FOREIGN KEY (team_id) REFERENCES teams (id);

ALTER TABLE league_teams ADD COLUMN total_cheer_count INT NOT NULL DEFAULT 0;
ALTER TABLE league_teams ADD COLUMN total_talk_count INT NOT NULL DEFAULT 0;
ALTER TABLE league_teams ADD COLUMN ranking INT NULL;
ALTER TABLE league_teams
    ADD CONSTRAINT uc_league_team UNIQUE (league_id, team_id);

-- league_team_players 테이블 삭제
DROP TABLE IF EXISTS league_team_players;

-- lineup_players 테이블 변경: players 테이블 참조하도록
ALTER TABLE lineup_players DROP COLUMN name;
ALTER TABLE lineup_players DROP COLUMN description;
ALTER TABLE lineup_players DROP COLUMN league_team_player_id;

ALTER TABLE lineup_players ADD COLUMN player_id BIGINT NOT NULL;
ALTER TABLE lineup_players
    ADD CONSTRAINT FK_LINEUP_PLAYERS_ON_PLAYERS FOREIGN KEY (player_id) REFERENCES players (id);

ALTER TABLE lineup_players CHANGE number jersey_number INT NULL;
ALTER TABLE lineup_players
    ADD CONSTRAINT uc_lineup_player UNIQUE (game_team_id, player_id);

-- timelines 테이블 변경: quarters 테이블 삭제에 따라 quarter_id → quarter (VARCHAR)로 변경
ALTER TABLE timelines DROP FOREIGN KEY FK_TIMELINES_ON_RECORDED_QUARTER;
ALTER TABLE timelines DROP COLUMN recorded_quarter_id;
ALTER TABLE timelines ADD COLUMN recorded_quarter VARCHAR(255) NOT NULL;

ALTER TABLE timelines DROP FOREIGN KEY FK_TIMELINES_ON_PREV_QUARTER;
ALTER TABLE timelines DROP COLUMN previous_quarter_id;
ALTER TABLE timelines ADD COLUMN previous_quarter VARCHAR(255) NULL;

ALTER TABLE timelines MODIFY COLUMN type VARCHAR(255) NOT NULL;
ALTER TABLE timelines MODIFY COLUMN is_success BOOLEAN NULL;

-- 새로운 통계 테이블 생성
CREATE TABLE league_top_scorers
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    league_id  BIGINT                NOT NULL,
    player_id  BIGINT                NOT NULL,
    ranking    INT                   NOT NULL,
    goal_count INT                   NOT NULL DEFAULT 0,

    CONSTRAINT pk_league_top_scorers PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUE_TOP_SCORERS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_LEAGUE_TOP_SCORERS_ON_PLAYERS FOREIGN KEY (player_id) REFERENCES players (id)
);

CREATE TABLE league_statistics
(
    id                       BIGINT AUTO_INCREMENT NOT NULL,
    league_id                BIGINT                NOT NULL,
    first_winner_team_id     BIGINT                NULL,
    second_winner_team_id    BIGINT                NULL,
    most_cheered_team_id     BIGINT                NULL,
    most_cheer_talks_team_id BIGINT                NULL,

    CONSTRAINT pk_league_statistics PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_FIRST_WINNER FOREIGN KEY (first_winner_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_SECOND_WINNER FOREIGN KEY (second_winner_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_MOST_CHEERED FOREIGN KEY (most_cheered_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_MOST_TALKS FOREIGN KEY (most_cheer_talks_team_id) REFERENCES teams (id)
);
