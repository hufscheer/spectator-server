package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

public record LeagueTeamResponse(
        Long teamId,
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfTeamPlayers
) {
    public LeagueTeamResponse(final Team team, final Long leagueTeamId) {
        this(
                team.getId(), leagueTeamId, team.getName(),
                team.getLogoImageUrl(), team.getTeamPlayers().size()
        );
    }
}
