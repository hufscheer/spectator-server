package com.sports.server.game.dto.response;

import com.sports.server.game.domain.GameTeam;

public record GameTeamCheerResponseDto(
        Long gameTeamId,
        int cheerCount
) {
    public GameTeamCheerResponseDto(final GameTeam gameTeam) {
        this(gameTeam.getId(), gameTeam.getCheerCount());
    }

}
