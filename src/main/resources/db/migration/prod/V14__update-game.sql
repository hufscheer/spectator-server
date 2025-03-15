ALTER TABLE games
    MODIFY COLUMN round VARCHAR(255);

UPDATE games
SET round = CASE
                WHEN round = '2' THEN '결승'
                WHEN round = '4' THEN '4강'
                WHEN round = '8' THEN '8강'
                WHEN round = '16' THEN '16강'
                WHEN round = '32' THEN '32강'
    END;