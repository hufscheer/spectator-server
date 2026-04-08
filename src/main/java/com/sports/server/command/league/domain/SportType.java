package com.sports.server.command.league.domain;

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
    };

    public abstract Quarter resolveQuarter(String value);

    public abstract Quarter firstQuarter();

    public abstract Quarter postGameQuarter();
}
