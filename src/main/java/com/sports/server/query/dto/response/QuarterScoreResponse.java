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

    public static QuarterScoreResponse of(Quarter quarter, Map<Long, Long> teamScoreMap) {
        List<TeamScore> scores = teamScoreMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TeamScore(e.getKey(), e.getValue().intValue()))
                .toList();
        return new QuarterScoreResponse(quarter.name(), quarter.getDisplayName(), scores);
    }
}
