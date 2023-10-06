package com.sports.server.game.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sports.server.game.domain.Game;
import com.sports.server.record.dto.response.RecordResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GameDetailResponseDto extends GameResponseDto {

    private List<RecordResponseDto> records;

    public GameDetailResponseDto(Game game) {
        super(game);
        this.records = game.getRecords().stream().map(RecordResponseDto::new).toList();
    }
}
