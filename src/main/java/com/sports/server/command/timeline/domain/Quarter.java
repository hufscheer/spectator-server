package com.sports.server.command.timeline.domain;

import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.Getter;

@Getter
public enum Quarter {
    PRE_GAME("경기전", 0),
    FIRST_HALF("전반전", 1),
    SECOND_HALF("후반전", 2),
    EXTRA_TIME("연장전", 3),
    PENALTY_SHOOTOUT("승부차기", 4),
    POST_GAME("경기후", 5);

    private final String name;
    private final Integer order;

    Quarter(String name, Integer order) {
        this.name = name;
        this.order = order;
    }

    public static Quarter fromName(String name) {
        for (Quarter quarter : Quarter.values()) {
            if (quarter.name.equals(name)) {
                return quarter;
            }
        }
        throw new BadRequestException(String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, name));
    }

    public static Quarter resolve(String value) {
        for (Quarter quarter : Quarter.values()) {
            if (quarter.name().equals(value) || quarter.name.equals(value)) {
                return quarter;
            }
        }
        throw new BadRequestException(String.format(ExceptionMessages.QUARTER_NOT_FOUND_BY_NAME, value));
    }


    public boolean isOrder(Integer order) {
        return this.order.equals(order);
    }

    public boolean isPreviousThan(Quarter other) {
        return this.order < other.order;
    }
}