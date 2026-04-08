package com.sports.server.command.timeline.domain;

import lombok.Getter;

@Getter
public enum BasketballQuarter implements Quarter {
    FIRST_QUARTER("1쿼터", 1),
    SECOND_QUARTER("2쿼터", 2),
    THIRD_QUARTER("3쿼터", 3),
    FOURTH_QUARTER("4쿼터", 4),
    OVERTIME("연장전", 5);

    private final String displayName;
    private final int order;

    BasketballQuarter(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }
}
