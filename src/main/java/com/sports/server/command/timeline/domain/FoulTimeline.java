package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue("FOUL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoulTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer fouledPlayer;

    public FoulTimeline(Game game,
                        Quarter recordedQuarter,
                        Integer recordedAt,
                        LineupPlayer fouledPlayer
    ) {
        super(game, recordedQuarter, recordedAt);
        this.fouledPlayer = fouledPlayer;
    }

    @Override
    public TimelineType getType() {
        return TimelineType.FOUL;
    }

    @Override
    public void apply() {
        game.issueFoul(fouledPlayer);
    }

    @Override
    public void rollback() {
        game.cancelFoul(fouledPlayer);
    }
}