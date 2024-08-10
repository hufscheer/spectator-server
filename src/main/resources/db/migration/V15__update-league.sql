UPDATE leagues
SET max_round         = COALESCE(max_round, '16강'),
    in_progress_round = COALESCE(in_progress_round, '결승');
