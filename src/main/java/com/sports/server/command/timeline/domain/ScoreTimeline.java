package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.LineupPlayer;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("score")
@Getter
public class ScoreTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Column(name = "score")
    private Integer score;
}
