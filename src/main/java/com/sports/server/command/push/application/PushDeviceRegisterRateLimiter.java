package com.sports.server.command.push.application;

public interface PushDeviceRegisterRateLimiter {
    void check(String clientId);
}
