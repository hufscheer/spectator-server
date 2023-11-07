-- comments -> game_teams 외래키 제약 제거
ALTER TABLE comments DROP FOREIGN KEY FK_COMMENTS_ON_GAME_TEAM;

-- comments.game_team_id에 인덱스 추가
CREATE INDEX IDX_COMMENTS_ON_GAME_TEAM_ID ON comments (game_team_id);

-- games에 name 추가
ALTER TABLE games ADD name VARCHAR(255) NOT NULL;
