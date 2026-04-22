package com.sports.server.command.league.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum SoccerQuarter implements Quarter {
    FIRST_HALF("전반전", 1),
    SECOND_HALF("후반전", 2),
    EXTRA_TIME("연장전", 3),
    PENALTY_SHOOTOUT("승부차기", 4);

    private final String displayName;
    private final int order;

    SoccerQuarter(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    @Override
    public Quarter firstQuarter() {
        return FIRST_HALF;
    }

    @Override
    public boolean canEndGame() {
        return this == SECOND_HALF || this == EXTRA_TIME || this == PENALTY_SHOOTOUT;
    }

    @Override
    public boolean canHaveQuarterEnd() {
        return this != PENALTY_SHOOTOUT;
    }

    @Override
    public boolean canEndGameAfterQuarterEnd() {
        return this != SECOND_HALF && canEndGame();
    }

    public static Optional<SoccerQuarter> tryResolve(String value) {
        for (SoccerQuarter quarter : values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
                return Optional.of(quarter);
            }
        }
        return Optional.empty();
    }

    public static SoccerQuarter resolve(String value) {
        return tryResolve(value)
                .orElseThrow(() -> new BadRequestException(
                        String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value)));
    }
}
