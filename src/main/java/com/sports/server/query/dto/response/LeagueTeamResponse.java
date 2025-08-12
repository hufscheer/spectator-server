package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

public record LeagueTeamResponse(
        Long teamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfTeamPlayers
) {
    public LeagueTeamResponse(final Team team) {
        this(
                team.getId(), team.getName(), team.getLogoImageUrl(),
                team.getTeamPlayers().size()
        );
    }
}
