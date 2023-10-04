package com.sports.server.record.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sports.server.record.domain.Record;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RecordResponseDto {

    private Long id;

    private Long teamId;

    private String playerName;

    private int score;

    private LocalDateTime scoredAt;


    public RecordResponseDto(final Record record) {
        this.id = record.getId();
        this.teamId = record.getTeam().getId();
        this.playerName = record.getPlayerName();
        this.score = 1; // TODO: 추후 경기 종목 테이블 추가에 따른 리팩토링
        this.scoredAt = record.getScoredAt();
    }
}
