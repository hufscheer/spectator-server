package com.sports.server.command.game.dto;

public record CheerCountUpdateRequest(
        Long gameTeamId,
        int cheerCount
) {
}
