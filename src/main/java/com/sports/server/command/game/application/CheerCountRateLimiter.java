package com.sports.server.command.game.application;

public interface CheerCountRateLimiter {
    void check(String clientId);
}
