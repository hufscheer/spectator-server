package com.sports.server.command.timeline.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;

@JsonDeserialize(using = QuarterDeserializer.class)
public interface Quarter {

    String name();

    String getDisplayName();

    int getOrder();

    default boolean isPreviousThan(Quarter other) {
        return this.getOrder() < other.getOrder();
    }

    static Quarter resolve(String value) {
        for (SoccerQuarter quarter : SoccerQuarter.values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
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

    static Quarter resolve(SportType sportType, String value) {
        return switch (sportType) {
            case SOCCER -> resolveSoccer(value);
            case BASKETBALL -> resolveBasketball(value);
        };
    }

    private static Quarter resolveSoccer(String value) {
        for (SoccerQuarter quarter : SoccerQuarter.values()) {
            if (quarter.name().equals(value) || quarter.getDisplayName().equals(value)) {
                return quarter;
            }
        }
        throw new BadRequestException(String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value));
    }

    private static Quarter resolveBasketball(String value) {
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