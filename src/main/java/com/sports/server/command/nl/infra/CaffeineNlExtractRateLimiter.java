package com.sports.server.command.nl.infra;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_RATE_LIMIT_EXCEEDED;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.nl.application.NlExtractRateLimiter;
import com.sports.server.command.nl.exception.NlRateLimitException;
import com.sports.server.common.util.SlidingWindow;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CaffeineNlExtractRateLimiter implements NlExtractRateLimiter {

    private static final int LIMIT = 10;
    private static final long WINDOW_NANOS = TimeUnit.MINUTES.toNanos(10);
    // window의 1.5배 — 경계 구간 안전 버퍼
    private static final long TTL_MINUTES = 15L;
    private static final long MAX_SIZE = 10_000L;

    private final Ticker ticker;
    private final Cache<Long, SlidingWindow> windows;

    public CaffeineNlExtractRateLimiter() {
        this(Ticker.systemTicker());
    }

    CaffeineNlExtractRateLimiter(Ticker ticker) {
        this.ticker = ticker;
        this.windows = Caffeine.newBuilder()
                .expireAfterWrite(TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAX_SIZE)
                .ticker(ticker)
                .build();
    }

    @Override
    public void check(Long memberId) {
        SlidingWindow window = windows.get(memberId, k -> new SlidingWindow(WINDOW_NANOS, LIMIT));
        if (!window.tryAdmit(ticker.read())) {
            throw new NlRateLimitException(EXTRACT_RATE_LIMIT_EXCEEDED);
        }
    }
}
