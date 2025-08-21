package com.sports.server.command.team.dto;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

import java.util.List;

public class TeamRequest {
    public record Register(
            String name,
            String logoImageUrl,
            Unit unit,
            String teamColor,
            List<TeamPlayerRegister> teamPlayers
    ) {
        public Team toEntity(String logoImageUrl) {
            return Team.builder()
                    .name(this.name)
                    .logoImageUrl(logoImageUrl)
                    .unit(this.unit)
                    .teamColor(this.teamColor)
                    .build();
        }
    }

    public record Update(
            String name,
            String logoImageUrl,
            Unit unit,
            String teamColor
    ) {}

    public record TeamPlayerRegister(
            Long playerId,
            Integer jerseyNumber
    ){}

}
