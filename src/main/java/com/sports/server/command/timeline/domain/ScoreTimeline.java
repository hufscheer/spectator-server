package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("SCORE")
@Getter
public class ScoreTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Column(name = "score")
    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team1_id")
    private GameTeam gameTeam1;

    @Column(name = "snapshot_score1")
    private Integer snapshotScore1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team2_id")
    private GameTeam gameTeam2;

    @Column(name = "snapshot_score2")
    private Integer snapshotScore2;

    @Override
    public String getType() {
        return "SCORE";
    }
}
