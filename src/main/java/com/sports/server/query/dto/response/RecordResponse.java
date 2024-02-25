package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.ReplacementRecord;
import com.sports.server.command.record.domain.ScoreRecord;
import com.sports.server.command.sport.domain.Quarter;

public record RecordResponse(
        @JsonIgnore
        Quarter quarter,
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
                record.getRecordedQuarter(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                replacementRecord.getOriginLineupPlayer().getName(),
                team.getName(),
                team.getLogoImageUrl(),
                null,
                new ReplacementRecordResponse(replacementRecord.getReplacedLineupPlayer().getName())
        );
    }

    public static RecordResponse from(ScoreRecord scoreRecord, ScoreRecordResponse scoreRecordResponse) {
        Record record = scoreRecord.getRecord();
        LeagueTeam team = record.getGameTeam().getLeagueTeam();
        return new RecordResponse(
                record.getRecordedQuarter(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                scoreRecord.getLineupPlayer().getName(),
                team.getName(),
                team.getLogoImageUrl(),
                scoreRecordResponse,
                null
        );
    }
}
