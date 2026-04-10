package com.sports.server.command.league.domain;

import java.util.Arrays;

public enum SportType {
    SOCCER {
        @Override
        public Quarter resolveQuarter(String value) {
            return SoccerQuarter.resolve(value);
        }

        @Override
        public Quarter firstQuarter() {
            return SoccerQuarter.FIRST_HALF;
        }

        @Override
        public Quarter postGameQuarter() {
            return SoccerQuarter.POST_GAME;
        }

        @Override
        public Quarter quarterByOrder(int order) {
            return Arrays.stream(SoccerQuarter.values())
                    .filter(q -> q.getOrder() == order)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당 순서의 쿼터가 없습니다: " + order));
        }
    },
    BASKETBALL {
        @Override
        public Quarter resolveQuarter(String value) {
            return BasketballQuarter.resolve(value);
        }

        @Override
        public Quarter firstQuarter() {
            return BasketballQuarter.FIRST_QUARTER;
        }

        @Override
        public Quarter postGameQuarter() {
            return BasketballQuarter.POST_GAME;
        }

        @Override
        public Quarter quarterByOrder(int order) {
            return Arrays.stream(BasketballQuarter.values())
                    .filter(q -> q.getOrder() == order)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당 순서의 쿼터가 없습니다: " + order));
        }
    };

    public abstract Quarter resolveQuarter(String value);

    public abstract Quarter firstQuarter();

    public abstract Quarter postGameQuarter();

    public abstract Quarter quarterByOrder(int order);
}
