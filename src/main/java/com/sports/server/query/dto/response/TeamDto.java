package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamDto {

    private Long id;

    private String name;

    private String logoImageUrl;

    public TeamDto(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.logoImageUrl = team.getLogoImageUrl();
    }
}
