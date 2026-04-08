package com.sports.server.command.timeline.domain;

import lombok.Getter;

@Getter
public enum SoccerQuarter implements Quarter {
    PRE_GAME("경기전", 0),
    FIRST_HALF("전반전", 1),
    SECOND_HALF("후반전", 2),
    EXTRA_TIME("연장전", 3),
    PENALTY_SHOOTOUT("승부차기", 4),
    POST_GAME("경기후", 5);

    private final String displayName;
    private final int order;

    SoccerQuarter(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }
}