package com.sports.server.query.application;

import com.sports.server.query.dto.response.GameDetailResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;

import java.util.List;
import java.util.Map;

public record TeamStatistics(
        Map<Long, TeamDetailResponse.TeamGameResult> gameResultsMap,
        Map<Long, List<TeamDetailResponse.TeamTopScorer>> topScorersMap,
        Map<Long, List<TeamDetailResponse.Trophy>> trophiesMap,
        Map<Long, List<GameDetailResponse>> recentGamesMap)
{
}
