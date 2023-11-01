package com.sports.server.game.dto.response;

import com.sports.server.game.domain.GameTeam;
import com.sports.server.team.dto.TeamDto;
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
