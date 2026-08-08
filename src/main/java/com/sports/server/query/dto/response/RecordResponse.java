package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
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
        OwnGoalRecordResponse ownGoalRecord,
        ReplacementRecordResponse replacementRecord,
        ProgressRecordResponse progressRecord,
        PkRecordResponse pkRecord,
        WarningCardRecordResponse warningCardRecord
) {
    public static RecordResponse from(Timeline timeline) {
        Optional<LineupPlayer> lineupPlayer = getPlayer(timeline);
        Optional<GameTeam> gameTeam = getCreditedGameTeam(timeline, lineupPlayer);
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
                timeline instanceof OwnGoalTimeline ownGoalTimeline
                        ? OwnGoalRecordResponse.from(ownGoalTimeline) : null,
                timeline instanceof ReplacementTimeline replacementTimeline
                        ? new ReplacementRecordResponse(
                                replacementTimeline.getId(),
                                replacementTimeline.getReplacedPlayerName(),
                                timeline instanceof BasketballReplacementTimeline b ? b.isFoulOut() : null) : null,
                timeline instanceof GameProgressTimeline progressTimeline
                        ? new ProgressRecordResponse(progressTimeline.getGameProgressType()) : null,
                timeline instanceof PKTimeline pkTimeline
                        ? new PkRecordResponse(pkTimeline.getId(), pkTimeline.getIsSuccess()) : null,
                timeline instanceof WarningCardTimeline warningCardTimeline
                        ? new WarningCardRecordResponse(warningCardTimeline.getWarningCardType()) : null
        );
    }

    private static Optional<GameTeam> getCreditedGameTeam(Timeline timeline, Optional<LineupPlayer> lineupPlayer) {
        if (timeline instanceof OwnGoalTimeline ownGoalTimeline) {
            return Optional.of(resolveOpponentTeam(ownGoalTimeline));
        }
        return lineupPlayer.map(LineupPlayer::getGameTeam);
    }

    // 자책골의 경우에는 타임라인이 등록 주체의 상대 팀으로 표기됨
    private static GameTeam resolveOpponentTeam(OwnGoalTimeline ownGoalTimeline) {
        GameTeam ownTeam = ownGoalTimeline.getScorer().getGameTeam();
        if (ownTeam.equals(ownGoalTimeline.getGameTeam1())) {
            return ownGoalTimeline.getGameTeam2();
        }
        return ownGoalTimeline.getGameTeam1();
    }

    private static Optional<LineupPlayer> getPlayer(Timeline timeline) {
        if (timeline instanceof ScoreTimeline scoreTimeline) {
            return Optional.of(scoreTimeline.getScorer());
        } else if (timeline instanceof OwnGoalTimeline ownGoalTimeline) {
            return Optional.of(ownGoalTimeline.getScorer());
        } else if (timeline instanceof ReplacementTimeline replacementTimeline) {
            return Optional.of(replacementTimeline.getOriginLineupPlayer());
        } else if (timeline instanceof PKTimeline pkTimeline) {
            return Optional.of(pkTimeline.getScorer());
        } else if (timeline instanceof WarningCardTimeline warningCardTimeline) {
            return Optional.of(warningCardTimeline.getScorer());
        } else if (timeline instanceof FoulTimeline foulTimeline) {
            return Optional.of(foulTimeline.getOffender());
        }
        return Optional.empty();
    }
}
