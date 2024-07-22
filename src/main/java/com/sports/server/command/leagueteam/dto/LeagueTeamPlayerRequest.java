package com.sports.server.command.leagueteam.dto;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;

public class LeagueTeamPlayerRequest {
    public record Register(
            String name,
            int number
    ) {
        public LeagueTeamPlayer toEntity(LeagueTeam leagueTeam) {
            return new LeagueTeamPlayer(leagueTeam, name, number);
        }
    }

    public record Update(
            Long id,
            String name,
            int number
    ) {

    }
}
