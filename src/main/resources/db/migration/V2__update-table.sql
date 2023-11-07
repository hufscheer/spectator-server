-- comments -> game_teams 외래키 제약 제거
ALTER TABLE comments DROP FOREIGN KEY FK_COMMENTS_ON_GAME_TEAM;

-- games에 name 추가
ALTER TABLE games ADD name VARCHAR(255) NOT NULL;
