package com.sports.server.command.team.dto;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

public class TeamRequest {
    public record Register(
            String name,
            String logoImageUrl,
            Unit unit,
            String teamColor
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

    public record PlayerIdRequest(
            Long playerId
    ) {}

}
