package com.sports.server.common.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SlidingWindow {

    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final long windowNanos;
    private final int limit;

    public SlidingWindow(long windowNanos, int limit) {
        this.windowNanos = windowNanos;
        this.limit = limit;
    }

    public synchronized boolean tryAdmit(long nowNanos) {
        evictExpired(nowNanos);
        if (timestamps.size() >= limit) {
            return false;
        }
        timestamps.addLast(nowNanos);
        return true;
    }

    private void evictExpired(long nowNanos) {
        long threshold = nowNanos - windowNanos;
        while (!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
            timestamps.pollFirst();
        }
    }
}
