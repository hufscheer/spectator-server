package com.sports.server.command.game.dto.response;

import com.sports.server.command.team.dto.TeamDto;
import com.sports.server.command.game.domain.GameTeam;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameTeamDto extends TeamDto {
    private int score;

    public GameTeamDto(GameTeam gameTeam) {
        super(gameTeam.getTeam());
        this.score = gameTeam.getScore();
    }
}
