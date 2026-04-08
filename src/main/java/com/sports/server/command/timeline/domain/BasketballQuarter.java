package com.sports.server.command.timeline.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
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

    public static Quarter resolve(String value) {
        for (SoccerQuarter quarter : SoccerQuarter.values()) {
            if ((quarter == SoccerQuarter.PRE_GAME || quarter == SoccerQuarter.POST_GAME)
                    && (quarter.name().equals(value) || quarter.getDisplayName().equals(value))) {
                return quarter;
            }
        }
        for (BasketballQuarter quarter : BasketballQuarter.values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
                return quarter;
            }
        }
        throw new BadRequestException(String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value));
    }
}
