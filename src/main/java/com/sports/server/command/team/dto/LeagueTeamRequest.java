package com.sports.server.command.team.dto;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.member.domain.Member;
import java.util.List;

public class LeagueTeamRequest {
    public record Register(
            String name,
            String logoImageUrl,
            List<LeagueTeamPlayerRequest.Register> players,
            String teamColor
    ) {

        public LeagueTeam toEntity(Member manager, League league, String logoImageUrl) {
            return new LeagueTeam(name, logoImageUrl, manager, league, teamColor);
        }
    }

    public record Update(
            String name,
            String logoImageUrl,
            List<LeagueTeamPlayerRequest.Register> newPlayers,
            List<LeagueTeamPlayerRequest.Update> updatedPlayers,
            List<Long> deletedPlayerIds
    ) {

    }

}
