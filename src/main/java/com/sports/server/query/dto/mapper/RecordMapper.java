package com.sports.server.query.dto.mapper;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.ReplacementRecord;
import com.sports.server.command.record.domain.ScoreRecord;
import com.sports.server.query.application.timeline.ScoreSnapshot;
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
        GameTeam gameTeam = record.getGameTeam();
        LeagueTeam team = gameTeam.getLeagueTeam();
        return new RecordResponse(
                record.getRecordedQuarter(),
                record.getId(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                replacementRecord.getOriginLineupPlayer().getName(),
                gameTeam.getId(),
                team.getName(),
                team.getLogoImageUrl(),
                null,
                new ReplacementRecordResponse(replacementRecord.getId(), replacementRecord.getReplacedLineupPlayer().getName())
        );
    }

    public RecordResponse toRecordResponse(ScoreRecord scoreRecord, ScoreSnapshot snapshot) {
        Record record = scoreRecord.getRecord();
        int score = scoreRecord.getScore();
        GameTeam gameTeam = record.getGameTeam();
        LeagueTeam team = gameTeam.getLeagueTeam();
        return new RecordResponse(
                record.getRecordedQuarter(),
                record.getId(),
                record.getRecordType().name(),
                record.getRecordedAt(),
                scoreRecord.getLineupPlayer().getName(),
                gameTeam.getId(),
                team.getName(),
                team.getLogoImageUrl(),
                new ScoreRecordResponse(scoreRecord.getId(), score, toSnapshotResponses(snapshot)),
                null
        );
    }

    private List<ScoreRecordResponse.Snapshot> toSnapshotResponses(ScoreSnapshot snapshot) {
        return snapshot.getTeamsOrderById()
                .stream()
                .map(gameTeam -> toSnapshotResponse(snapshot, gameTeam))
                .toList();
    }

    private ScoreRecordResponse.Snapshot toSnapshotResponse(ScoreSnapshot snapshot,
                                                            GameTeam gameTeam) {
        LeagueTeam leagueTeam = gameTeam.getLeagueTeam();
        return new ScoreRecordResponse.Snapshot(
                leagueTeam.getName(),
                leagueTeam.getLogoImageUrl(),
                snapshot.getScore(gameTeam)
        );
    }
}
