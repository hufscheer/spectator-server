package com.sports.server.command.timeline.domain;

import java.util.List;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "assist_lineup_player_id")
    private LineupPlayer assistLineupPlayer;

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
            LineupPlayer scorer,
            LineupPlayer assistLineupPlayer,
            int scoreValue
    ) {
        if (assistLineupPlayer != null
                && (!scorer.isSameTeam(assistLineupPlayer) || scorer.getId().equals(assistLineupPlayer.getId()))) {
            throw new BadRequestException(ExceptionMessages.INVALID_ASSIST_PLAYER);
        }
        if (assistLineupPlayer != null
                && game.getSportType() == SportType.BASKETBALL
                && scoreValue == BasketballScore.ONE.getValue()) {
            throw new BadRequestException(ExceptionMessages.INVALID_FREE_THROW_ASSIST);
        }

        GameTeam team1 = game.getTeam1();
        GameTeam team2 = game.getTeam2();

        return new ScoreTimeline(
                game,
                recordedQuarter,
                recordedAt,
                scorer,
                assistLineupPlayer,
                scoreValue,
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
            LineupPlayer assistLineupPlayer,
            Integer score,
            GameTeam gameTeam1,
            Integer snapshotScore1,
            GameTeam gameTeam2,
            Integer snapshotScore2
    ) {
        super(game, recordedQuarter, recordedAt);

        this.scorer = scorer;
        this.assistLineupPlayer = assistLineupPlayer;
        this.score = score;
        this.gameTeam1 = gameTeam1;
        this.snapshotScore1 = snapshotScore1;
        this.gameTeam2 = gameTeam2;
        this.snapshotScore2 = snapshotScore2;
    }

    @Override
    public void apply() {
        game.score(scorer, score);

        snapshotScore1 = gameTeam1.getScore();
        snapshotScore2 = gameTeam2.getScore();
    }

    @Override
    public void rollback() {
        game.cancelScore(scorer, score);
    }

    /**
     * 어시스트 선수는 넣지 않는다. 기획상 어시스트는 교체 삭제 판정에서 "이후 기록에 등장" 으로
     * 보지 않기 때문이다 — 교체로 들어온 선수가 이후 어시스트만 했다면 그 교체는 삭제할 수 있다.
     */
    @Override
    public List<LineupPlayer> getRelatedLineupPlayers() {
        return List.of(scorer);
    }
}
