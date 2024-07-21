ALTER TABLE leagues
    MODIFY COLUMN max_round VARCHAR(255);
ALTER TABLE leagues
    MODIFY COLUMN in_progress_round VARCHAR(255);

UPDATE leagues
    SET max_round = CASE
        WHEN max_round = '2' THEN '결승'
        WHEN max_round = '4' THEN '4강'
        WHEN max_round = '8' THEN '8강'
        WHEN max_round = '16' THEN '16강'
        WHEN max_round = '32' THEN '32강'
    END,
    in_progress_round = CASE
        WHEN in_progress_round = '2' THEN '결승'
        WHEN in_progress_round = '4' THEN '4강'
        WHEN in_progress_round = '8' THEN '8강'
        WHEN in_progress_round = '16' THEN '16강'
        WHEN in_progress_round = '32' THEN '32강'
    END;
