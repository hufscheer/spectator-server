package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.timeline.domain.ScoreTimeline;

import java.util.List;

public record ScoreRecordResponse(
        Long scoreRecordId,
        Integer score,
        List<Snapshot> snapshot
) {

    public static ScoreRecordResponse from(ScoreTimeline scoreTimeline) {
        return new ScoreRecordResponse(
                scoreTimeline.getId(),
                scoreTimeline.getScore(),
                List.of(
                        Snapshot.of(scoreTimeline.getGameTeam1(), scoreTimeline.getSnapshotScore1()),
                        Snapshot.of(scoreTimeline.getGameTeam2(), scoreTimeline.getSnapshotScore2())
                )
        );
    }

    public record Snapshot(
            String teamName,
            String teamImageUrl,
            Integer score
    ) {
        public static Snapshot of(GameTeam gameTeam, Integer score) {
            return new Snapshot(
                    gameTeam.getTeam().getName(),
                    gameTeam.getTeam().getLogoImageUrl(),
                    score
            );
        }
    }
}
