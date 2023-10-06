package com.sports.server.game.dto.response;

import com.sports.server.game.domain.Game;
import com.sports.server.record.domain.Record;
import com.sports.server.record.dto.response.RecordResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Getter
@NoArgsConstructor
public class GameDetailResponseDto extends GameResponseDto {

    private List<RecordResponseDto> records;

    public GameDetailResponseDto(Game game) {
        super(game);
        this.records = game.getRecords()
                .stream()
                .sorted(Comparator.comparing(Record::getScoredAt))
                .map(RecordResponseDto::new)
                .toList();

    }
}
