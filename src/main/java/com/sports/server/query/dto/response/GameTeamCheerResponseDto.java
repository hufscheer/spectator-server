package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;

public record GameTeamCheerResponseDto(
        Long gameTeamId,
        int cheerCount
) {
    public GameTeamCheerResponseDto(final GameTeam gameTeam) {
        this(gameTeam.getId(), gameTeam.getCheerCount());
    }

}
