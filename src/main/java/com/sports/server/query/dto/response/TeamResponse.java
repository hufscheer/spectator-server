package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

public record TeamResponse(
        Long id,
        String name,
        String logoImageUrl,
        String unit,
        String teamColor
) {
    public TeamResponse(final Team team){
        this(
                team.getId(),
                team.getName(),
                team.getLogoImageUrl(),
                team.getUnit().getName(),
                team.getTeamColor()
        );
    }
}