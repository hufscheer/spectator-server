package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;

public record RecordResponse(
        @JsonIgnore
        Quarter quarter,
        Long recordId,
        String type,
        Integer recordedAt,
        String playerName,
        Long gameTeamId,
        String teamName,
        String teamImageUrl,
        ScoreRecordResponse scoreRecord,
        ReplacementRecordResponse replacementRecord
) {
    public static RecordResponse from(Timeline timeline) {
        LineupPlayer lineupPlayer = getPlayerName(timeline);
        GameTeam gameTeam = lineupPlayer.getGameTeam();
        LeagueTeam leagueTeam = gameTeam.getLeagueTeam();

        return new RecordResponse(
                timeline.getRecordedQuarter(),
                timeline.getId(),
                timeline.getType(),
                timeline.getRecordedAt(),
                lineupPlayer.getName(),
                gameTeam.getId(),
                leagueTeam.getName(),
                leagueTeam.getLogoImageUrl(),
                timeline instanceof ScoreTimeline scoreTimeline
                        ? ScoreRecordResponse.from(scoreTimeline) : null,
                timeline instanceof ReplacementTimeline replacementTimeline
                        ? ReplacementRecordResponse.from(replacementTimeline) : null
        );
    }

    private static LineupPlayer getPlayerName(Timeline timeline) {
        if (timeline instanceof ScoreTimeline scoreTimeline) {
            return scoreTimeline.getScorer();
        } else if (timeline instanceof ReplacementTimeline replacementTimeline) {
            return replacementTimeline.getOriginLineupPlayer();
        }
        return null;
    }
}
