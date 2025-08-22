package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.team.domain.Team;

public record LeagueTeamResponse(
        Long teamId,
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer sizeOfTeamPlayers,
        Integer cheerCount,
        Integer cheerTalksCount
) {
    public LeagueTeamResponse(final Team team, final Long leagueTeamId) {
        this(
                team.getId(), leagueTeamId, team.getName(),
                team.getLogoImageUrl(), team.getTeamPlayers().size(),
                0, 0
        );
    }

    public LeagueTeamResponse(final LeagueTeam leagueTeam) {
        this(
                leagueTeam.getTeam().getId(),
                leagueTeam.getId(),
                leagueTeam.getTeam().getName(),
                leagueTeam.getTeam().getLogoImageUrl(),
                leagueTeam.getTeam().getTeamPlayers().size(),
                leagueTeam.getTotalCheerCount(),
                leagueTeam.getTotalTalkCount()
        );
    }
}
