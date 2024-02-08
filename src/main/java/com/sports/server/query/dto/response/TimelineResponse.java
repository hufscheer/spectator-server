package com.sports.server.query.dto.response;

import com.sports.server.command.record.domain.Record;
import com.sports.server.command.sport.domain.Quarter;

import java.util.List;

public record TimelineResponse(
        String gameQuarter,
        List<RecordResponse> records
) {

    public TimelineResponse(Quarter quarter, List<Record> records) {
        this(
                quarter.getName(),
                records.stream()
                        .map(RecordResponse::new)
                        .toList()
        );
    }

    public record RecordResponse(
            Integer scoredAt,
            String playerName,
            String teamName,
            Integer score
    ) {
        public RecordResponse(Record record) {
            this(
                    record.getRecordedAt(),
                    record.getLineupPlayer().getName(),
                    record.getGameTeam().getLeagueTeam().getName(),
                    record.getScore()
            );
        }
    }
}