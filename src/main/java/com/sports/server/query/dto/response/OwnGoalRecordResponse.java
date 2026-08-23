package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import java.util.List;

public record OwnGoalRecordResponse(
        Long ownGoalRecordId,
        Integer score,
        List<ScoreRecordResponse.Snapshot> snapshot
) {

    public static OwnGoalRecordResponse from(OwnGoalTimeline ownGoalTimeline) {
        return new OwnGoalRecordResponse(
                ownGoalTimeline.getId(),
                ownGoalTimeline.getScore(),
                List.of(
                        ScoreRecordResponse.Snapshot.of(ownGoalTimeline.getGameTeam1(), ownGoalTimeline.getSnapshotScore1()),
                        ScoreRecordResponse.Snapshot.of(ownGoalTimeline.getGameTeam2(), ownGoalTimeline.getSnapshotScore2())
                )
        );
    }
}
