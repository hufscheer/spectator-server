package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("BASKETBALL_REPLACEMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasketballReplacementTimeline extends ReplacementTimeline {

    @Column(name = "is_foul_out", nullable = false)
    private boolean isFoulOut;

    public BasketballReplacementTimeline(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer originLineupPlayer,
            LineupPlayer replacedLineupPlayer,
            boolean isFoulOut
    ) {
        super(game, recordedQuarter, recordedAt, originLineupPlayer, replacedLineupPlayer);
        this.isFoulOut = isFoulOut;
    }

    @Override
    public TimelineType getType() {
        return TimelineType.BASKETBALL_REPLACEMENT;
    }
}
