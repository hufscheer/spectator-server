package com.sports.server.command.league.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum CommonQuarter implements Quarter {
    PRE_GAME("경기전", 0),
    POST_GAME("경기후", 99);

    private final String displayName;
    private final int order;

    CommonQuarter(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    @Override
    public Quarter firstQuarter() {
        throw new UnsupportedOperationException("CommonQuarter does not have a firstQuarter");
    }

    @Override
    public boolean canEndGame() {
        return false;
    }

    @Override
    public boolean canHaveQuarterEnd() {
        return false;
    }

    public static Optional<CommonQuarter> tryResolve(String value) {
        for (CommonQuarter quarter : values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
                return Optional.of(quarter);
            }
        }
        return Optional.empty();
    }

    public static CommonQuarter resolve(String value) {
        return tryResolve(value)
                .orElseThrow(() -> new BadRequestException(
                        String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value)));
    }
}
