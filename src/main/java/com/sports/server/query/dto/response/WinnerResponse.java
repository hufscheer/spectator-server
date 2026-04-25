package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;

public record WinnerResponse(
        Long gameTeamId,
        String teamName,
        String teamImageUrl
) {
    public static WinnerResponse from(GameTeam gameTeam) {
        return new WinnerResponse(
                gameTeam.getId(),
                gameTeam.getTeam().getName(),
                gameTeam.getTeam().getLogoImageUrl()
        );
    }
}
