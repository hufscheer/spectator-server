package com.sports.server.command.game.dto;

public record GameTeamCheerRequestDto(
        Long gameTeamId,
        int cheerCount
) {
}
