package com.sports.server.common.log;

@FunctionalInterface
public interface ThrowableCallable<V> {

    V call() throws Throwable;
}
