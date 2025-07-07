-- 1단계: 더미 학번을 NULL로 정리
UPDATE league_team_players
SET student_number = NULL
WHERE student_number = '000000000';

-- 2단계: 유효한 학번으로 NULL 값 채우기
UPDATE league_team_players t1
SET student_number = (
    SELECT t2.student_number
    FROM league_team_players t2
    WHERE t2.name = t1.name
      AND t2.student_number IS NOT NULL
    LIMIT 1
    )
WHERE t1.student_number IS NULL
  AND EXISTS (
    SELECT 1
    FROM league_team_players t3
    WHERE t3.name = t1.name
  AND t3.student_number IS NOT NULL
    );