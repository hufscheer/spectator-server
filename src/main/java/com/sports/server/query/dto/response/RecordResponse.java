package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.ReplacementRecord;

public record RecordResponse(
        @JsonIgnore
        Long quarterId,
        String type,
        Integer recordedAt,
        String playerName,
        String teamName,
        String teamImageUrl,
        ScoreRecordResponse score,
        ReplacementRecordResponse replacement
) {

    public static RecordResponse from(ReplacementRecord replacementRecord) {
        Record record = replacementRecord.getRecord();
        LeagueTeam team = record.getGameTeam().getLeagueTeam();
        return new RecordResponse(
                record.getRecordedQuarter().getId(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                replacementRecord.getOriginLineupPlayer().getName(),
                team.getName(),
                team.getLogoImageUrl(),
                null,
                new ReplacementRecordResponse(replacementRecord.getReplacedLineupPlayer().getName())
        );
    }
}
