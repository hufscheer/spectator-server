package com.sports.server.command.push.infra;

import static com.sports.server.command.push.exception.PushErrorMessages.DEVICE_REGISTER_RATE_LIMIT_EXCEEDED;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.push.application.PushDeviceRegisterRateLimiter;
import com.sports.server.command.push.exception.PushRateLimitException;
import com.sports.server.common.util.SlidingWindow;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CaffeinePushDeviceRegisterRateLimiter implements PushDeviceRegisterRateLimiter {

    private static final int RATE_LIMIT = 20;
    private static final long RATE_WINDOW_NANOS = TimeUnit.MINUTES.toNanos(10);
    // window의 1.5배 — 경계 구간 안전 버퍼
    private static final long RATE_TTL_SECONDS = 900L;
    private static final long RATE_MAX_SIZE = 10_000L;

    private static final String UNKNOWN_CLIENT = "unknown";

    private final Ticker ticker;
    private final Cache<String, SlidingWindow> rateWindows;

    public CaffeinePushDeviceRegisterRateLimiter() {
        this(Ticker.systemTicker());
    }

    CaffeinePushDeviceRegisterRateLimiter(Ticker ticker) {
        this.ticker = ticker;
        this.rateWindows = Caffeine.newBuilder()
                .expireAfterWrite(RATE_TTL_SECONDS, TimeUnit.SECONDS)
                .maximumSize(RATE_MAX_SIZE)
                .ticker(ticker)
                .build();
    }

    @Override
    public void check(String clientId) {
        long now = ticker.read();
        if (!rateWindow(normalizeId(clientId)).tryAdmit(now)) {
            throw new PushRateLimitException(DEVICE_REGISTER_RATE_LIMIT_EXCEEDED);
        }
    }

    private SlidingWindow rateWindow(String clientId) {
        return rateWindows.get(clientId, k -> new SlidingWindow(RATE_WINDOW_NANOS, RATE_LIMIT));
    }

    private static String normalizeId(String clientId) {
        return (clientId == null || clientId.isBlank()) ? UNKNOWN_CLIENT : clientId;
    }
}
