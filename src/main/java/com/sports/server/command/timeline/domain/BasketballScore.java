package com.sports.server.command.timeline.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.common.exception.BadRequestException;
import java.util.Arrays;

public enum BasketballScore {
    ONE(1), TWO(2), THREE(3);

    private final int value;

    BasketballScore(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static BasketballScore fromValue(int value) {
        return Arrays.stream(values())
                .filter(s -> s.value == value)
                .findFirst()
                .orElseThrow(() -> new BadRequestException(TimelineErrorMessage.INVALID_BASKETBALL_SCORE));
    }
}
