package com.sports.server.command.league.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum BasketballQuarter implements Quarter {
    PRE_GAME("경기전", 0),
    FIRST_QUARTER("1쿼터", 1),
    SECOND_QUARTER("2쿼터", 2),
    THIRD_QUARTER("3쿼터", 3),
    FOURTH_QUARTER("4쿼터", 4),
    OVERTIME("연장전", 5),
    POST_GAME("경기후", 6);

    private final String displayName;
    private final int order;

    BasketballQuarter(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    @Override
    public Quarter firstQuarter() {
        return FIRST_QUARTER;
    }

    @Override
    public boolean canEndGame() {
        return this == FOURTH_QUARTER || this == OVERTIME;
    }

    public static Optional<BasketballQuarter> tryResolve(String value) {
        for (BasketballQuarter quarter : values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
                return Optional.of(quarter);
            }
        }
        return Optional.empty();
    }

    public static BasketballQuarter resolve(String value) {
        return tryResolve(value)
                .orElseThrow(() -> new BadRequestException(
                        String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value)));
    }
}
