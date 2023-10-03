package com.sports.server.game.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sports.server.game.domain.Game;
import com.sports.server.team.domain.Team;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameRegisterRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String sportsName;

    @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm")
    private LocalDateTime startTime;

    private Long firstTeamId;

    private Long secondTeamId;

    public Game toEntity(final Team firstTeam, final Team secondTeam) {
        return Game.builder()
                .name(this.name)
                .sportsName(this.sportsName)
                .startTime(this.startTime)
                .firstTeam(firstTeam)
                .secondTeam(secondTeam)
                .firstTeamScore(0)
                .secondTeamScore(0)
                .build();
    }

}
