package com.sports.server.query.dto;

public record PlayerAssistCountWithRank(
        Long playerId,
        String studentNumber,
        String playerName,
        Long assistCount,
        Long rank
) {
}
