package com.sports.server.command.game.dto.request;

public record GameTeamCheerRequestDto(
        Long gameTeamId,
        int cheerCount
) {
}
