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

    static Quarter firstOf(SportType sportType) {
        return switch (sportType) {
            case SOCCER -> SoccerQuarter.FIRST_HALF;
            case BASKETBALL -> BasketballQuarter.FIRST_QUARTER;
        };
    }

    static Quarter resolve(SportType sportType, String value) {
        return switch (sportType) {
            case SOCCER -> SoccerQuarter.resolve(value);
            case BASKETBALL -> BasketballQuarter.resolve(value);
        };
    }
}