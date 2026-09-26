package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.timeline.domain.ScoreTimeline;

import java.util.List;

public record ScoreRecordResponse(
        Long scoreRecordId,
        Integer score,
        List<Snapshot> snapshot,
        String assistPlayerName
) {

    public static ScoreRecordResponse from(ScoreTimeline scoreTimeline) {
        LineupPlayer assist = scoreTimeline.getAssistLineupPlayer();
        return new ScoreRecordResponse(
                scoreTimeline.getId(),
                scoreTimeline.getScore(),
                List.of(
                        Snapshot.of(scoreTimeline.getGameTeam1(), scoreTimeline.getSnapshotScore1()),
                        Snapshot.of(scoreTimeline.getGameTeam2(), scoreTimeline.getSnapshotScore2())
                ),
                assist != null ? assist.getPlayer().getName() : null
        );
    }

    public record Snapshot(
            String teamName,
            String teamImageUrl,
            Integer score
    ) {
        // 지워진 팀이면 team 이 null 이다. 매니저 화면이 snapshot[0]·[1] 의 점수를 읽으므로 칸은 남긴다
        public static Snapshot of(GameTeam gameTeam, Integer score) {
            Team team = gameTeam.getTeam();
            return new Snapshot(
                    team != null ? team.getName() : null,
                    team != null ? team.getLogoImageUrl() : null,
                    score
            );
        }
    }
}
