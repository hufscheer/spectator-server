package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.Quarter;

import java.util.List;
import java.util.Map;

public record QuarterScoreResponse(
        String quarter,
        String displayName,
        List<TeamScore> scores
) {
    public record TeamScore(Long gameTeamId, int score) {}

    public static QuarterScoreResponse of(Quarter quarter, List<Long> gameTeamIds, Map<Long, Integer> teamScoreMap) {
        List<TeamScore> scores = gameTeamIds.stream()
                .map(id -> new TeamScore(id, teamScoreMap.getOrDefault(id, 0)))
                .toList();
        return new QuarterScoreResponse(quarter.name(), quarter.getDisplayName(), scores);
    }
}
