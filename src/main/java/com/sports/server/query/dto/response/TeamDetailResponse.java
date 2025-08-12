package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

import java.util.List;

public record TeamDetailResponse(
        String name,
        String logoImageUrl,
        String unit,
        String teamColor,
        List<PlayerResponse> teamPlayers
) {
    public TeamDetailResponse(final Team team, final List<PlayerResponse> teamPlayers){
        this(
                team.getName(),
                team.getLogoImageUrl(),
                team.getUnit().getName(),
                team.getTeamColor(),
                teamPlayers
        );
    }
}
