package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.LineupPlayer;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("PK")
@Getter
public class PKTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Override
    public String getType() {
        return "PK";
    }
}
