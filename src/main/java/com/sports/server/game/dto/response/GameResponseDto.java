package com.sports.server.game.dto.response;

import com.sports.server.game.domain.Game;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameResponseDto {
    private Long id;

    private List<GameTeamDto> teams;

    private String gameQuarter;

    public GameResponseDto(Game game) {
        this.id = game.getId();
        this.teams = game.getTeams().stream().map(GameTeamDto::new).toList();
        this.gameQuarter = game.getGameQuarter();
    }
}
