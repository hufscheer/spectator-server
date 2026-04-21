package com.sports.server.command.team.dto;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

import java.util.List;

public class TeamRequest {
    public record Register(
            String name,
            String logoImageUrl,
            String unit,
            String teamColor,
            List<TeamPlayerRegister> teamPlayers,
            SportType sportType
    ) {
        public Team toEntity(String logoImageUrl, Unit unit) {
            return Team.builder()
                    .name(this.name)
                    .logoImageUrl(logoImageUrl)
                    .unit(unit)
                    .teamColor(this.teamColor)
                    .sportType(this.sportType)
                    .build();
        }
    }

    public record Update(
            String name,
            String logoImageUrl,
            String unit,
            String teamColor,
            List<TeamPlayerRegister> teamPlayers
    ) {}

    public record TeamPlayerRegister(
            Long playerId,
            Integer jerseyNumber
    ){}

}
