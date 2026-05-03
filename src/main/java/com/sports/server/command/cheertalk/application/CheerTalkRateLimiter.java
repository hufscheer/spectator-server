package com.sports.server.command.cheertalk.application;

public interface CheerTalkRateLimiter {

    void check(String clientId, String content);
}
