package com.sports.server.game.dto.request;

public record GameTeamCheerRequestDto(
        Long gameTeamId,
        int cheerCount
) {
}
