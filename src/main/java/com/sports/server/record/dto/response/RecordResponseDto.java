package com.sports.server.record.dto.response;

import com.sports.server.record.domain.Record;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RecordResponseDto {

    private Long id;
    private Long teamId;
    private String playerName;
    private int score;

    public RecordResponseDto(final Record record) {
        this.id = record.getId();
        this.teamId = record.getTeam().getId();
        this.playerName = record.getPlayerName();
        this.score = 1; // TODO: 추후 경기 종목 테이블 추가에 따른 리팩토링
    }
}
