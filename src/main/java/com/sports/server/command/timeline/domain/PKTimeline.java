package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("PK")
@Getter
public class PKTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Override
    public TimelineType getType() {
        return TimelineType.PK;
    }

    public PKTimeline(Game game,
                      Quarter recordedQuarter, Integer recordedAt,
                      LineupPlayer scorer, Boolean isSuccess) {
        super(game, recordedQuarter, recordedAt);
        this.scorer = scorer;
        this.isSuccess = isSuccess;
    }

    @Override
    public void apply() {
        game.scoreInPk(scorer);
    }

    @Override
    public void rollback() {
        game.cancelPkScore(scorer);
    }
}
