package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

public record TeamResponse(
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfLeagueTeamPlayers
) {
    public TeamResponse(final Team team) {
        this(
                team.getId(), team.getName(), team.getLogoImageUrl(),
                team.getTeamPlayers().size()
        );
    }
}
