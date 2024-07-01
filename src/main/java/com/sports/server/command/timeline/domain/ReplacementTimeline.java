package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.LineupPlayer;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("REPLACEMENT")
@Getter
public class ReplacementTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_lineup_player_id")
    private LineupPlayer originLineupPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_lineup_player_id")
    private LineupPlayer replacedLineupPlayer;

    @Override
    public String getType() {
        return "REPLACEMENT";
    }
}
