package com.sports.server.game.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sports.server.common.Constants;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameStatus;
import com.sports.server.team.dto.TeamDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameResponseDto {
    private Long id;

    private String name;

    private String sportsName;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT)
    private LocalDateTime startTime;

    private TeamDto firstTeam;

    private TeamDto secondTeam;

    private int firstTeamScore;

    private int secondTeamScore;

    private GameStatus gameStatus;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT)
    private LocalDateTime statusChangedAt;

    public GameResponseDto(Game game) {
        this.id = game.getId();
        this.name = game.getName();
        this.sportsName = game.getSportsName();
        this.startTime = game.getStartTime();
        this.firstTeam = new TeamDto(game.getFirstTeam());
        this.secondTeam = new TeamDto(game.getSecondTeam());
        this.firstTeamScore = game.getFirstTeamScore();
        this.secondTeamScore = game.getSecondTeamScore();
        this.gameStatus = game.getGameStatus();
        this.statusChangedAt = game.getStatusChangedAt();
    }
}
