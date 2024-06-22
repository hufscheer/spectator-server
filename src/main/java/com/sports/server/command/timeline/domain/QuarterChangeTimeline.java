package com.sports.server.command.timeline.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Entity
@DiscriminatorValue("QUARTER_CHANGE")
@Getter
public class QuarterChangeTimeline extends Timeline  {

    @Enumerated(EnumType.STRING)
    private QuarterChangeType quarterChangeType;
}
