-- comments -> game_teams 외래키 제약 제거
ALTER TABLE comments DROP FOREIGN KEY FK_COMMENTS_ON_GAME_TEAM;

-- comments.game_team_id에 인덱스 추가
CREATE INDEX IDX_COMMENTS_ON_GAME_TEAM_ID ON comments (game_team_id);

-- games에 name, state 추가
ALTER TABLE games ADD name VARCHAR(255) NOT NULL;
ALTER TABLE games ADD state VARCHAR(255) NOT NULL;

-- meber_id -> administrator_id 컬럼명 변경
ALTER TABLE games CHANGE member_id administrator_id BIGINT;

-- reports 상태 칼럼 추가
ALTER TABLE reports ADD COLUMN state VARCHAR(255);
ALTER TABLE reports DROP COLUMN is_valid;

-- 댓글과 신고 1대1 매핑
ALTER TABLE reports ADD CONSTRAINT uc_reports_comment UNIQUE (comment_id);
