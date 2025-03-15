CREATE TABLE organizations
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);

CREATE TABLE members
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    organization_id  BIGINT                NULL,
    email            VARCHAR(255)          NOT NULL,
    password         VARCHAR(255)          NOT NULL,
    is_administrator BIT(1)                NOT NULL,
    last_login       datetime              NULL,
    CONSTRAINT pk_members PRIMARY KEY (id)
);

ALTER TABLE members
    ADD CONSTRAINT uc_members_email UNIQUE (email);

ALTER TABLE members
    ADD CONSTRAINT FK_MEMBERS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE TABLE teams
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255)          NOT NULL,
    logo_image_url   VARCHAR(255)          NOT NULL,
    administrator_id BIGINT                NULL,
    organization_id  BIGINT                NULL,
    CONSTRAINT pk_teams PRIMARY KEY (id)
);

ALTER TABLE teams
    ADD CONSTRAINT FK_TEAMS_ON_ADMINISTRATOR FOREIGN KEY (administrator_id) REFERENCES members (id);

ALTER TABLE teams
    ADD CONSTRAINT FK_TEAMS_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE TABLE sports
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_sports PRIMARY KEY (id)
);

CREATE TABLE quarters
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    name      VARCHAR(255)          NOT NULL,
    sports_id BIGINT                NULL,
    CONSTRAINT pk_quarters PRIMARY KEY (id)
);

ALTER TABLE quarters
    ADD CONSTRAINT FK_QUARTERS_ON_SPORTS FOREIGN KEY (sports_id) REFERENCES sports (id);

CREATE TABLE leagues
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    administrator_id BIGINT                NULL,
    organization_id  BIGINT                NULL,
    name             VARCHAR(255)          NOT NULL,
    start_at         datetime              NOT NULL,
    end_at           datetime              NOT NULL,
    CONSTRAINT pk_leagues PRIMARY KEY (id)
);

ALTER TABLE leagues
    ADD CONSTRAINT FK_LEAGUES_ON_ADMINISTRATOR FOREIGN KEY (administrator_id) REFERENCES members (id);

ALTER TABLE leagues
    ADD CONSTRAINT FK_LEAGUES_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organizations (id);

CREATE TABLE league_sports
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    sport_id  BIGINT                NULL,
    league_id BIGINT                NULL,
    CONSTRAINT pk_league_sports PRIMARY KEY (id)
);

ALTER TABLE league_sports
    ADD CONSTRAINT FK_LEAGUE_SPORTS_ON_LEAGUE FOREIGN KEY (league_id) REFERENCES leagues (id);

ALTER TABLE league_sports
    ADD CONSTRAINT FK_LEAGUE_SPORTS_ON_SPORT FOREIGN KEY (sport_id) REFERENCES sports (id);

CREATE TABLE games
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    sport_id           BIGINT                NULL,
    member_id          BIGINT                NULL,
    league_id          BIGINT                NULL,
    start_time         datetime              NOT NULL,
    video_id           VARCHAR(255)          NULL,
    quarter_changed_at datetime              NULL,
    game_quarter       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_games PRIMARY KEY (id)
);

ALTER TABLE games
    ADD CONSTRAINT FK_GAMES_ON_LEAGUE FOREIGN KEY (league_id) REFERENCES leagues (id);

ALTER TABLE games
    ADD CONSTRAINT FK_GAMES_ON_MEMBER FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE games
    ADD CONSTRAINT FK_GAMES_ON_SPORT FOREIGN KEY (sport_id) REFERENCES sports (id);

CREATE TABLE game_teams
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    game_id     BIGINT                NULL,
    team_id     BIGINT                NULL,
    cheer_count INT                   NOT NULL,
    score       INT                   NOT NULL,
    CONSTRAINT pk_game_teams PRIMARY KEY (id)
);

ALTER TABLE game_teams
    ADD CONSTRAINT FK_GAME_TEAMS_ON_GAME FOREIGN KEY (game_id) REFERENCES games (id);

ALTER TABLE game_teams
    ADD CONSTRAINT FK_GAME_TEAMS_ON_TEAM FOREIGN KEY (team_id) REFERENCES teams (id);

CREATE TABLE game_team_players
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    game_team_id  BIGINT                NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` VARCHAR(255)          NULL,
    CONSTRAINT pk_game_team_players PRIMARY KEY (id)
);

ALTER TABLE game_team_players
    ADD CONSTRAINT FK_GAME_TEAM_PLAYERS_ON_GAME_TEAM FOREIGN KEY (game_team_id) REFERENCES game_teams (id);

CREATE TABLE comments
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime              NOT NULL,
    content      VARCHAR(255)          NOT NULL,
    is_blocked   BIT(1)                NOT NULL,
    game_team_id BIGINT                NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_GAME_TEAM FOREIGN KEY (game_team_id) REFERENCES game_teams (id);

CREATE TABLE records
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    game_id             BIGINT                NULL,
    game_team_id        BIGINT                NULL,
    game_team_player_id BIGINT                NULL,
    score               INT                   NOT NULL,
    scored_quarter      VARCHAR(255)          NOT NULL,
    scored_at           datetime              NOT NULL,
    CONSTRAINT pk_records PRIMARY KEY (id)
);

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_GAME FOREIGN KEY (game_id) REFERENCES games (id);

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_GAME_TEAM FOREIGN KEY (game_team_id) REFERENCES game_teams (id);

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_GAME_TEAM_PLAYER FOREIGN KEY (game_team_player_id) REFERENCES game_team_players (id);

CREATE TABLE reports
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    comment_id  BIGINT                NULL,
    reported_at datetime              NOT NULL,
    is_valid    BIT(1)                NOT NULL,
    CONSTRAINT pk_reports PRIMARY KEY (id)
);

ALTER TABLE reports
    ADD CONSTRAINT FK_REPORTS_ON_COMMENT FOREIGN KEY (comment_id) REFERENCES comments (id);
