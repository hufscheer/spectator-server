SET foreign_key_checks = 0;

-- Comment 테이블에 데이터를 넣는 INSERT 쿼리
INSERT INTO `comments` (`created_at`, `content`, `is_blocked`, `game_team_id`)
VALUES ('2023-01-01 12:30:00', 'Great play!', false, 1),
       ('2023-01-02 14:45:00', 'What a save!', false, 2),
       ('2023-01-03 16:00:00', 'Nice teamwork!', true, 3),
       ('2023-01-04 18:15:00', 'Unbelievable goal!', false, 1);

SET foreign_key_checks = 1;