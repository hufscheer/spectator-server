-- 대진표(싱글 엘리미네이션 브래킷) 매치 테이블
-- round/match_number 로 트리 구조를 표현: round R 의 match m 승자는 다음 라운드 match ceil(m/2) 로 진출
-- team1_id/team2_id 는 1라운드(브래킷 크기 라운드) 배치에만 사용, 상위 라운드 진출팀은 경기 결과로부터 유도
CREATE TABLE bracket_matches
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    league_id    BIGINT                NOT NULL,
    round        VARCHAR(255)          NOT NULL,
    match_number INT                   NOT NULL,
    team1_id     BIGINT                NULL,
    team2_id     BIGINT                NULL,
    game_id      BIGINT                NULL,

    CONSTRAINT pk_bracket_matches PRIMARY KEY (id),
    CONSTRAINT FK_BRACKET_MATCHES_ON_LEAGUES FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT FK_BRACKET_MATCHES_ON_TEAM1 FOREIGN KEY (team1_id) REFERENCES teams (id),
    CONSTRAINT FK_BRACKET_MATCHES_ON_TEAM2 FOREIGN KEY (team2_id) REFERENCES teams (id),
    CONSTRAINT FK_BRACKET_MATCHES_ON_GAMES FOREIGN KEY (game_id) REFERENCES games (id),
    CONSTRAINT uc_bracket_match_position UNIQUE (league_id, round, match_number),
    CONSTRAINT uc_bracket_match_game UNIQUE (game_id)
);
