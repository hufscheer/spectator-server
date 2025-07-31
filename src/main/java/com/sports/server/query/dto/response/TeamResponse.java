package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

public record TeamResponse(
        Long id,
        String name,
        String logoImageUrl,
        Unit unit,
        String teamColor
) {
    public TeamResponse(final Team team){
        this(
                team.getId(),
                team.getName(),
                team.getLogoImageUrl(),
                team.getUnit(),
                team.getTeamColor()
        );
    }
}
