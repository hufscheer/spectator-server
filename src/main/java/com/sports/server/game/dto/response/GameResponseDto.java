package com.sports.server.game.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sports.server.game.domain.Game;
import com.sports.server.record.dto.response.RecordResponseDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameResponseDto {
    private Long id;

    private String name;

    private String sportsName;

    @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm")
    private LocalDateTime startTime;

    private int firstTeamScore;

    private int secondTeamScore;

    private List<RecordResponseDto> records;

    public GameResponseDto(Game game) {
        this.id = game.getId();
        this.name = game.getName();
        this.sportsName = game.getSportsName();
        this.startTime = game.getStartTime();
        this.firstTeamScore = game.getFirstTeamScore();
        this.secondTeamScore = game.getSecondTeamScore();
        this.records = game.getRecords().stream().map(RecordResponseDto::new).toList();
    }
}
