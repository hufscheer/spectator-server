package com.sports.server.game.dto.response;

import com.sports.server.game.domain.GameTeam;

public record GameTeamCheerResponseDto(
        Long gameTeamId,
        int cheerCount,
        int order
) {
    public GameTeamCheerResponseDto(final GameTeam gameTeam, final int order) {
        this(gameTeam.getId(), gameTeam.getCheerCount(), order);
    }

}
