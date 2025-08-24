package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.team.domain.Team;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
                null, null
        );
    }

    public static LeagueTeamResponse ofWithCheerCount(final LeagueTeam leagueTeam) {
        return new LeagueTeamResponse(
                leagueTeam.getTeam().getId(),
                leagueTeam.getId(),
                leagueTeam.getTeam().getName(),
                leagueTeam.getTeam().getLogoImageUrl(),
                leagueTeam.getTeam().getTeamPlayers().size(),
                leagueTeam.getTotalCheerCount(),
                null
        );
    }

    public static LeagueTeamResponse ofWithTotalTalkCount(final LeagueTeam leagueTeam) {
        return new LeagueTeamResponse(
                leagueTeam.getTeam().getId(),
                leagueTeam.getId(),
                leagueTeam.getTeam().getName(),
                leagueTeam.getTeam().getLogoImageUrl(),
                leagueTeam.getTeam().getTeamPlayers().size(),
                null,
                leagueTeam.getTotalTalkCount()
        );
    }
}
