package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SOCCER_REPLACEMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoccerReplacementTimeline extends ReplacementTimeline {

    public SoccerReplacementTimeline(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer originLineupPlayer,
            LineupPlayer replacedLineupPlayer
    ) {
        super(game, recordedQuarter, recordedAt, originLineupPlayer, replacedLineupPlayer);
    }

    @Override
    public TimelineType getType() {
        return TimelineType.SOCCER_REPLACEMENT;
    }
}
