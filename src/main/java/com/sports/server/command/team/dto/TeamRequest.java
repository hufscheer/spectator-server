package com.sports.server.command.team.dto;

import com.sports.server.command.league.domain.League;
;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

import java.util.List;

public class TeamRequest {
    public record Register(
            String name,
            String logoImageUrl,
            List<LeagueTeamPlayerRequest.Register> players,
            String teamColor
    ) {

        public Team toEntity(String name, Member administrator, Unit unit, String logoImageUrl) {
            return new Team(name, unit, logoImageUrl, administrator);
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
