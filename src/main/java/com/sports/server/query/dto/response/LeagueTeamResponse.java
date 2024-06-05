package com.sports.server.query.dto.response;

import com.sports.server.command.leagueteam.LeagueTeam;

public record LeagueTeamResponse(
        Long leagueTeamId,
        String teamName,
        String logoImageUrl
) {
    public LeagueTeamResponse(final LeagueTeam leagueTeam) {
        this(
                leagueTeam.getId(), leagueTeam.getName(), leagueTeam.getLogoImageUrl()
        );
    }
}
