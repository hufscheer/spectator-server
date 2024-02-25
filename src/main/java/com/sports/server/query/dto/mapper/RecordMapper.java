package com.sports.server.query.dto.mapper;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.ReplacementRecord;
import com.sports.server.command.record.domain.ScoreRecord;
import com.sports.server.query.application.ScoreSnapshot;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ReplacementRecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

    public RecordResponse toRecordResponse(ScoreRecord scoreRecord, ScoreSnapshot snapshot) {
        Record record = scoreRecord.getRecord();
        int score = scoreRecord.getScore();
        LeagueTeam team = record.getGameTeam().getLeagueTeam();

        List<ScoreRecordResponse.Snapshot> histories = toHistoryResponses(snapshot);
        return new RecordResponse(
                record.getRecordedQuarter(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                scoreRecord.getLineupPlayer().getName(),
                team.getName(),
                team.getLogoImageUrl(),
                new ScoreRecordResponse(score, histories),
                null
        );
    }

    private List<ScoreRecordResponse.Snapshot> toHistoryResponses(ScoreSnapshot snapshot) {
        return snapshot.getTeamsOrderById()
                .stream()
                .map(gameTeam -> toHistoryResponse(snapshot, gameTeam))
                .toList();
    }

    private ScoreRecordResponse.Snapshot toHistoryResponse(ScoreSnapshot snapshot,
                                                           GameTeam gameTeam) {
        LeagueTeam leagueTeam = gameTeam.getLeagueTeam();
        return new ScoreRecordResponse.Snapshot(
                leagueTeam.getName(),
                leagueTeam.getLogoImageUrl(),
                snapshot.getScore(gameTeam)
        );
    }
}
