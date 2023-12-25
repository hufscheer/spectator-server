package com.sports.server.common.log;

@FunctionalInterface
public interface ThrowableRunnable {

    void run() throws Throwable;
}
