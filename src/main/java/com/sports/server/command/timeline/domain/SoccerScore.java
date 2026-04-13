package com.sports.server.command.timeline.domain;

public enum SoccerScore {
    GOAL(1);

    private final int value;

    SoccerScore(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
