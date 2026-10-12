package com.sports.server.command.nl.application;

public interface NlExtractRateLimiter {

    void check(Long memberId);
}
