package com.sports.server.record.dto.response;

import com.sports.server.record.domain.Record;
import com.sports.server.sport.domain.Quarter;

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
                    record.getScoredAt(),
                    record.getGameTeamPlayer().getName(),
                    record.getGameTeam().getTeam().getName(),
                    record.getScore()
            );
        }
    }
}