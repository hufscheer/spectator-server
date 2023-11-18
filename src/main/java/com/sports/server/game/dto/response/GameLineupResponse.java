package com.sports.server.game.dto.response;

import java.util.List;

public record GameLineupResponse(
        Long gameTeamId,
        String teamName,
        List<PlayerResponse> gameTeamPlayers

) {

    public record PlayerResponse(
            String playerName,
            String description
    ) {
    }
}
