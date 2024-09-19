package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;

import java.util.Optional;

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
        ReplacementRecordResponse replacementRecord,
        ProgressRecordResponse progressRecord
) {
    public static RecordResponse from(Timeline timeline) {
        Optional<LineupPlayer> lineupPlayer = getPlayer(timeline);
        Optional<GameTeam> gameTeam = lineupPlayer.map(LineupPlayer::getGameTeam);
        Optional<LeagueTeam> leagueTeam = gameTeam.map(GameTeam::getLeagueTeam);

        return new RecordResponse(
                timeline.getRecordedQuarter(),
                timeline.getId(),
                timeline.getType().name(),
                timeline.getRecordedAt(),
                lineupPlayer.map(LineupPlayer::getName).orElse(null),
                gameTeam.map(GameTeam::getId).orElse(null),
                leagueTeam.map(LeagueTeam::getName).orElse(null),
                leagueTeam.map(LeagueTeam::getLogoImageUrl).orElse(null),
                timeline instanceof ScoreTimeline scoreTimeline
                        ? ScoreRecordResponse.from(scoreTimeline) : null,
                timeline instanceof ReplacementTimeline replacementTimeline
                        ? ReplacementRecordResponse.from(replacementTimeline) : null,
                timeline instanceof GameProgressTimeline progressTimeline
                        ? ProgressRecordResponse.from(progressTimeline) : null
        );
    }

    private static Optional<LineupPlayer> getPlayer(Timeline timeline) {
        if (timeline instanceof ScoreTimeline scoreTimeline) {
            return Optional.of(scoreTimeline.getScorer());
        } else if (timeline instanceof ReplacementTimeline replacementTimeline) {
            return Optional.of(replacementTimeline.getOriginLineupPlayer());
        }
        return Optional.empty();
    }
}
