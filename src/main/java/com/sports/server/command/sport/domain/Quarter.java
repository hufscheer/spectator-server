package com.sports.server.command.sport.domain;

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
        throw new IllegalArgumentException("Quarter not found for name: " + name);
    }

    public static Quarter fromId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Quarter ID cannot be null");
        }
        Quarter[] values = Quarter.values();
        if (id < 1 || id > values.length) {
            throw new IllegalArgumentException("Quarter not found for id: " + id);
        }
        return values[(int) (id - 1)];
    }

    public boolean isOrder(Integer order) {
        return this.order.equals(order);
    }

    public boolean isPreviousThan(Quarter other) {
        return this.order < other.order;
    }
}