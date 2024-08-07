package com.sports.server.query.dto.response;

import com.sports.server.command.leagueteam.domain.LeagueTeam;

public record LeagueTeamResponse(
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfLeagueTeamPlayers
) {
    public LeagueTeamResponse(final LeagueTeam leagueTeam) {
        this(
                leagueTeam.getId(), leagueTeam.getName(), leagueTeam.getLogoImageUrl(),
                leagueTeam.getLeagueTeamPlayers().size()
        );
    }
}
