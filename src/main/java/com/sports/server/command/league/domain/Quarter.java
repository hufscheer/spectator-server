package com.sports.server.command.league.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = QuarterDeserializer.class)
public interface Quarter {

    String name();

    String getDisplayName();

    int getOrder();

    Quarter firstQuarter();

    default boolean isPreviousThan(Quarter other) {
        return this.getOrder() < other.getOrder();
    }

    boolean canEndGame();

    boolean canHaveQuarterEnd();

    default boolean canEndGameAfterQuarterEnd() {
        return false;
    }
}
