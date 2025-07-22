package com.sports.server.command.team.dto;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;

public class LeagueTeamPlayerRequest {
    public record Register(
            String name,
            int number,
            String studentNumber
    ) {
        public LeagueTeamPlayer toEntity(LeagueTeam leagueTeam) {
            return new LeagueTeamPlayer(leagueTeam, name, number, studentNumber);
        }
    }

    public record Update(
            Long id,
            String name,
            int number,
            String studentNumber
    ) {

    }
}
