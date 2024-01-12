ALTER TABLE comments RENAME TO cheer_talks;
ALTER TABLE reports CHANGE comment_id cheer_talk_id BIGINT;

