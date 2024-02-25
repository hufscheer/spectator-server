package com.sports.server.query.dto.mapper;

import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.ReplacementRecord;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ReplacementRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecordMapper {

    public RecordResponse toRecordResponse(ReplacementRecord replacementRecord) {
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
}
