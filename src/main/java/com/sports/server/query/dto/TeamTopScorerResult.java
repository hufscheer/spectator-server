package com.sports.server.query.dto;

import com.sports.server.command.team.domain.PlayerGoalCountWithRank;

public record TeamTopScorerResult(
        Long teamId,
        PlayerGoalCountWithRank playerGoalCountWithRank
) {
}

