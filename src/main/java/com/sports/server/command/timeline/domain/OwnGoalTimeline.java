package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue("OWN_GOAL")
@Getter
@NoArgsConstructor
public class OwnGoalTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Column(name = "score")
    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "game_team1_id")
    private GameTeam gameTeam1;

    @Column(name = "snapshot_score1")
    private Integer snapshotScore1;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "game_team2_id")
    private GameTeam gameTeam2;

    @Column(name = "snapshot_score2")
    private Integer snapshotScore2;

    @Override
    public TimelineType getType() {
        return TimelineType.OWN_GOAL;
    }

    public static OwnGoalTimeline of(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer scorer,
            GameTeam requestedTeam,
            int scoreValue
    ) {
        if (!recordedQuarter.canRecordOwnGoal()) {
            throw new BadRequestException(ExceptionMessages.INVALID_OWN_GOAL_QUARTER);
        }
        if (!scorer.isInTeam(requestedTeam)) {
            throw new BadRequestException(ExceptionMessages.INVALID_OWN_GOAL_PLAYER);
        }

        GameTeam team1 = game.getTeam1();
        GameTeam team2 = game.getTeam2();

        return new OwnGoalTimeline(
                game,
                recordedQuarter,
                recordedAt,
                scorer,
                scoreValue,
                team1,
                team1.getScore(),
                team2,
                team2.getScore()
        );
    }

    private OwnGoalTimeline(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer scorer,
            Integer score,
            GameTeam gameTeam1,
            Integer snapshotScore1,
            GameTeam gameTeam2,
            Integer snapshotScore2
    ) {
        super(game, recordedQuarter, recordedAt);

        this.scorer = scorer;
        this.score = score;
        this.gameTeam1 = gameTeam1;
        this.snapshotScore1 = snapshotScore1;
        this.gameTeam2 = gameTeam2;
        this.snapshotScore2 = snapshotScore2;
    }

    @Override
    public void apply() {
        game.scoreOwnGoal(scorer, score);

        snapshotScore1 = gameTeam1.getScore();
        snapshotScore2 = gameTeam2.getScore();
    }

    @Override
    public void rollback() {
        game.cancelOwnGoalScore(scorer, score);
    }

    @Override
    public List<LineupPlayer> getRelatedLineupPlayers() {
        return List.of(scorer);
    }

    public GameTeam getOpponentGameTeam() {
        GameTeam ownTeam = scorer.getGameTeam();
        if (ownTeam.equals(gameTeam1)) {
            return gameTeam2;
        }
        return gameTeam1;
    }
}
