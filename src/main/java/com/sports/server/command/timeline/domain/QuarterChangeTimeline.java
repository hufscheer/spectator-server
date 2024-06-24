package com.sports.server.command.timeline.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@DiscriminatorValue("QUARTER_CHANGE")
@Getter
public class QuarterChangeTimeline extends Timeline  {

    @Enumerated(EnumType.STRING)
    @Column(name = "quarter_change_type")
    private QuarterChangeType quarterChangeType;
}
