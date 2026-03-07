package com.sports.server.command.nl.dto;

import java.util.List;

public record NlExecuteRequest(
        Long leagueId,
        Long teamId,
        List<PlayerData> players
) {
    public record PlayerData(
            String name,
            String studentNumber,
            Integer jerseyNumber
    ) {
    }
}
