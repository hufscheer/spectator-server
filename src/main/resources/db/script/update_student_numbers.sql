UPDATE league_team_players t1
SET student_number = (
    SELECT t2.student_number
    FROM league_team_players t2
    WHERE t2.name = t1.name
      AND t2.student_number IS NOT NULL
    ORDER BY t2.id DESC -- Assuming 'id' is the primary key
    LIMIT 1
    )
WHERE t1.student_number IS NULL;