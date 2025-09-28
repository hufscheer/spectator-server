package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.timeline.domain.Quarter;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.timeline.domain.*;

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
        ProgressRecordResponse progressRecord,
        PkRecordResponse pkRecord,
        WarningCardRecordResponse warningCardRecord
) {
    public static RecordResponse from(Timeline timeline) {
        Optional<LineupPlayer> lineupPlayer = getPlayer(timeline);
        Optional<GameTeam> gameTeam = lineupPlayer.map(LineupPlayer::getGameTeam);
        Optional<Team> team = gameTeam.map(GameTeam::getTeam);

        return new RecordResponse(
                timeline.getRecordedQuarter(),
                timeline.getId(),
                timeline.getType().name(),
                timeline.getRecordedAt(),
                lineupPlayer.map(lp -> lp.getPlayer().getName()).orElse(null),
                gameTeam.map(GameTeam::getId).orElse(null),
                team.map(Team::getName).orElse(null),
                team.map(Team::getLogoImageUrl).orElse(null),
                timeline instanceof ScoreTimeline scoreTimeline
                        ? ScoreRecordResponse.from(scoreTimeline) : null,
                timeline instanceof ReplacementTimeline replacementTimeline
                        ? new ReplacementRecordResponse(replacementTimeline.getId(), replacementTimeline.getReplacedPlayerName()) : null,
                timeline instanceof GameProgressTimeline progressTimeline
                        ? new ProgressRecordResponse(progressTimeline.getGameProgressType()) : null,
                timeline instanceof PKTimeline pkTimeline
                        ? new PkRecordResponse(pkTimeline.getId(), pkTimeline.getIsSuccess()) : null,
                timeline instanceof WarningCardTimeline warningCardTimeline
                        ? new WarningCardRecordResponse(warningCardTimeline.getWarningCardType()) : null
        );
    }

    private static Optional<LineupPlayer> getPlayer(Timeline timeline) {
        if (timeline instanceof ScoreTimeline scoreTimeline) {
            return Optional.of(scoreTimeline.getScorer());
        } else if (timeline instanceof ReplacementTimeline replacementTimeline) {
            return Optional.of(replacementTimeline.getOriginLineupPlayer());
        } else if (timeline instanceof PKTimeline pkTimeline) {
            return Optional.of(pkTimeline.getScorer());
        } else if (timeline instanceof WarningCardTimeline warningCardTimeline) {
            return Optional.of(warningCardTimeline.getScorer());
        }
        return Optional.empty();
    }
}
