package com.sports.server.command.leagueteam.dto;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;
import com.sports.server.command.member.domain.Member;
import java.util.List;

public record LeagueTeamRegisterRequest(
        String name,
        String logoImageUrl,
        List<LeagueTeamPlayerRegisterRequest> players
) {

    public LeagueTeam toEntity(Member manager, League league, String logoImageUrl) {
        return new LeagueTeam(name, logoImageUrl, manager, manager.getOrganization(), league);
    }

    public record LeagueTeamPlayerRegisterRequest(
            String name,
            int number
    ) {
        public LeagueTeamPlayer toEntity(LeagueTeam leagueTeam) {
            return new LeagueTeamPlayer(leagueTeam, name, number);
        }
    }
}
