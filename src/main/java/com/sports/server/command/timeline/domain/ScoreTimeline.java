package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
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
@DiscriminatorValue("SCORE")
@Getter
@NoArgsConstructor
public class ScoreTimeline extends Timeline {

    private static final int SCORE_VALUE = 1;

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
        return TimelineType.SCORE;
    }

    public static ScoreTimeline score(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer scorer
    ) {
        GameTeam team1 = game.getTeam1();
        GameTeam team2 = game.getTeam2();

        return new ScoreTimeline(
                game,
                recordedQuarter,
                recordedAt,
                scorer,
                SCORE_VALUE,
                team1,
                team1.getScore(),
                team2,
                team2.getScore()
        );
    }

    private ScoreTimeline(
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
        game.score(scorer);

        snapshotScore1 = gameTeam1.getScore();
        snapshotScore2 = gameTeam2.getScore();
    }

    @Override
    public void rollback() {
        game.cancelScore(scorer);
    }
}
