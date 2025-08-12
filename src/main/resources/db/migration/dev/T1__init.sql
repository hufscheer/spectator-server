CREATE TABLE organizations
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(255)          NOT NULL,

    CONSTRAINT pk_organizations      PRIMARY KEY (id)
);

CREATE TABLE members
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    organization_id  BIGINT                NULL,
    email            VARCHAR(255)          NOT NULL,
    password         VARCHAR(255)          NOT NULL,
    is_administrator BOOLEAN               NOT NULL,
    last_login       datetime              NULL,

    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT FK_MEMBER_ON_ORGANIZATIONS FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

CREATE TABLE teams
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    unit             VARCHAR(255)          NOT NULL,
    name             VARCHAR(255)          NOT NULL,
    logo_image_url   VARCHAR(255)          NULL,
    team_color       VARCHAR(255)          NOT NULL,

    CONSTRAINT pk_teams PRIMARY KEY (id)
);

CREATE TABLE players
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    student_number VARCHAR(255)          NOT NULL,

    CONSTRAINT pk_players PRIMARY KEY (id),
    CONSTRAINT uc_players_student_number UNIQUE (student_number)
);

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

CREATE TABLE leagues
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    organization_id   BIGINT                NOT NULL,
    administrator_id  BIGINT                NOT NULL,
    name              VARCHAR(255)          NOT NULL,
    start_at          DATETIME              NOT NULL,
    end_at            DATETIME              NOT NULL,
    is_deleted        BOOLEAN               NOT NULL DEFAULT FALSE,
    max_round         VARCHAR(255)          NULL,
    in_progress_round VARCHAR(255)          NULL,

    CONSTRAINT pk_leagues PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUES_ON_ORGANIZATIONS FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT FK_LEAGUES_ON_MEMBERS FOREIGN KEY (administrator_id) REFERENCES members (id)
);

CREATE TABLE games
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    administrator_id   BIGINT                NULL,
    league_id          BIGINT                NOT NULL,
    start_time         DATETIME              NOT NULL,
    name               VARCHAR(255)          NULL,
    video_id           VARCHAR(255)          NULL,
    quarter_changed_at DATETIME              NULL,
    game_quarter       VARCHAR(255)          NULL,
    state              VARCHAR(255)          NULL,
    round              VARCHAR(255)          NOT NULL,
    is_pk_taken        BOOLEAN               NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_games PRIMARY KEY (id),
    CONSTRAINT FK_GAMES_ON_MEMBERS FOREIGN KEY (administrator_id) REFERENCES members (id),
    CONSTRAINT FK_GAMES_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id)
);

CREATE TABLE game_teams
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    game_id     BIGINT                NOT NULL,
    team_id     BIGINT                NOT NULL,
    cheer_count INT                   NOT NULL DEFAULT 0,
    score       INT                   NOT NULL DEFAULT 0,
    pk_score    INT                   NOT NULL DEFAULT 0,
    result      VARCHAR(255)          NULL,

    CONSTRAINT pk_game_teams PRIMARY KEY (id),
    CONSTRAINT FK_GAME_TEAMS_ON_GAMES FOREIGN KEY (game_id) REFERENCES games (id),
    CONSTRAINT FK_GAME_TEAMS_ON_TEAMS FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT uc_game_team UNIQUE (game_id, team_id)
);

CREATE TABLE league_teams
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    league_id         BIGINT                NOT NULL,
    team_id           BIGINT                NOT NULL,
    total_cheer_count INT                   NOT NULL DEFAULT 0,
    total_talk_count  INT                   NOT NULL DEFAULT 0,
    ranking           INT                   NULL,

    CONSTRAINT pk_league_teams PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUE_TEAMS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_LEAGUE_TEAMS_ON_TEAMS FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT uc_league_team UNIQUE (league_id, team_id)
);

CREATE TABLE league_top_scorers
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    league_id   BIGINT                NOT NULL,
    player_id   BIGINT                NOT NULL,
    ranking     INT                   NOT NULL,
    goal_count  INT                   NOT NULL DEFAULT 0,

    CONSTRAINT pk_league_top_scorers PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUE_TOP_SCORERS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_LEAGUE_TOP_SCORERS_ON_PLAYERS FOREIGN KEY (player_id) REFERENCES players (id)
);

CREATE TABLE league_statistics
(
    id                         BIGINT AUTO_INCREMENT NOT NULL,
    league_id                  BIGINT                NOT NULL,
    first_winner_team_id       BIGINT                NULL,
    second_winner_team_id      BIGINT                NULL,
    most_cheered_team_id       BIGINT                NULL,
    most_cheer_talks_team_id   BIGINT                NULL,

    CONSTRAINT pk_league_statistics PRIMARY KEY (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_FIRST_WINNER FOREIGN KEY (first_winner_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_SECOND_WINNER FOREIGN KEY (second_winner_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_MOST_CHEERED FOREIGN KEY (most_cheered_team_id) REFERENCES teams (id),
    CONSTRAINT FK_LEAGUE_STATISTICS_ON_TEAMS_MOST_TALKS FOREIGN KEY (most_cheer_talks_team_id) REFERENCES teams (id)
);

CREATE TABLE lineup_players
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    game_team_id       BIGINT                NOT NULL,
    player_id          BIGINT                NOT NULL,
    jersey_number      INT                   NULL,
    is_captain         BOOLEAN               NOT NULL DEFAULT FALSE,
    state              VARCHAR(255)          NOT NULL,
    is_playing         BOOLEAN               NOT NULL DEFAULT TRUE,
    replaced_player_id BIGINT                NULL,

    CONSTRAINT pk_lineup_players PRIMARY KEY (id),
    CONSTRAINT FK_LINEUP_PLAYERS_ON_GAME_TEAMS FOREIGN KEY (game_team_id) REFERENCES game_teams (id),
    CONSTRAINT FK_LINEUP_PLAYERS_ON_PLAYERS FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT FK_LINEUP_PLAYERS_ON_REPLACED_PLAYER FOREIGN KEY (replaced_player_id) REFERENCES lineup_players (id),
    CONSTRAINT uc_lineup_player UNIQUE (game_team_id, player_id)
);

CREATE TABLE cheer_talks
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    game_team_id BIGINT                NOT NULL,
    content      VARCHAR(255)          NOT NULL,
    created_at   DATETIME              NOT NULL,
    is_blocked   BOOLEAN               NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_cheer_talks PRIMARY KEY (id),
    CONSTRAINT FK_CHEER_TALKS_ON_GAME_TEAMS FOREIGN KEY (game_team_id) REFERENCES game_teams (id)
);

CREATE TABLE reports
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    cheer_talk_id BIGINT                NOT NULL,
    reported_at   DATETIME              NOT NULL,
    state         VARCHAR(255)          NOT NULL,

    CONSTRAINT pk_reports PRIMARY KEY (id),
    CONSTRAINT FK_REPORTS_ON_CHEER_TALKS FOREIGN KEY (cheer_talk_id) REFERENCES cheer_talks (id)
);

CREATE TABLE timelines
(
    id                          BIGINT AUTO_INCREMENT NOT NULL,
    type                        VARCHAR(255)          NOT NULL,
    game_id                     BIGINT                NOT NULL,
    recorded_quarter            VARCHAR(255)          NOT NULL,
    recorded_at                 INT                   NOT NULL,
    scorer_id                   BIGINT                NULL,
    score                       INT                   NULL,
    is_success                  BOOLEAN               NULL,
    origin_lineup_player_id     BIGINT                NULL,
    replaced_lineup_player_id   BIGINT                NULL,
    game_progress_type          VARCHAR(255)          NULL,
    game_team1_id               BIGINT                NULL,
    game_team2_id               BIGINT                NULL,
    snapshot_score1             INT                   NULL,
    snapshot_score2             INT                   NULL,
    previous_quarter            VARCHAR(255)          NULL,
    previous_quarter_changed_at DATETIME              NULL,
    warning_card_type           VARCHAR(255)          NULL,

    CONSTRAINT pk_timelines PRIMARY KEY (id),
    CONSTRAINT FK_TIMELINES_ON_GAMES FOREIGN KEY (game_id) REFERENCES games (id),
    CONSTRAINT FK_TIMELINES_ON_GAME_TEAMS_1 FOREIGN KEY (game_team1_id) REFERENCES game_teams (id),
    CONSTRAINT FK_TIMELINES_ON_GAME_TEAMS_2 FOREIGN KEY (game_team2_id) REFERENCES game_teams (id),
    CONSTRAINT FK_TIMELINES_ON_ORIGIN_LINEUP_PLAYERS FOREIGN KEY (origin_lineup_player_id) REFERENCES lineup_players (id),
    CONSTRAINT FK_TIMELINES_ON_REPLACED_LINEUP_PLAYERS FOREIGN KEY (replaced_lineup_player_id) REFERENCES lineup_players (id),
    CONSTRAINT FK_TIMELINES_ON_LINEUP_PLAYERS_SCORER FOREIGN KEY (scorer_id) REFERENCES lineup_players (id)
);