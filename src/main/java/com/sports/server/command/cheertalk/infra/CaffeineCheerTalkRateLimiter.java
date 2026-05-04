package com.sports.server.command.cheertalk.infra;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_DUPLICATE_CONTENT;
import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_RATE_LIMIT_EXCEEDED;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.cheertalk.application.CheerTalkRateLimiter;
import com.sports.server.command.cheertalk.exception.CheerTalkRateLimitException;
import com.sports.server.common.util.SlidingWindow;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CaffeineCheerTalkRateLimiter implements CheerTalkRateLimiter {

    private static final int RATE_LIMIT = 120;
    private static final long RATE_WINDOW_NANOS = TimeUnit.SECONDS.toNanos(60);
    // window의 1.5배 — 경계 구간 안전 버퍼
    private static final long RATE_TTL_SECONDS = 90L;
    private static final long RATE_MAX_SIZE = 50_000L;

    private static final int DEDUP_LIMIT = 3;
    private static final long DEDUP_WINDOW_NANOS = TimeUnit.SECONDS.toNanos(3);
    // window의 2배 — 경계 구간 안전 버퍼
    private static final long DEDUP_TTL_SECONDS = 6L;
    private static final long DEDUP_MAX_SIZE = 100_000L;

    private static final String UNKNOWN_CLIENT = "unknown";

    private final Ticker ticker;
    private final Cache<String, SlidingWindow> rateWindows;
    private final Cache<DedupKey, SlidingWindow> dedupWindows;

    public CaffeineCheerTalkRateLimiter() {
        this(Ticker.systemTicker());
    }

    CaffeineCheerTalkRateLimiter(Ticker ticker) {
        this.ticker = ticker;
        this.rateWindows = Caffeine.newBuilder()
                .expireAfterWrite(RATE_TTL_SECONDS, TimeUnit.SECONDS)
                .maximumSize(RATE_MAX_SIZE)
                .ticker(ticker)
                .build();
        this.dedupWindows = Caffeine.newBuilder()
                .expireAfterWrite(DEDUP_TTL_SECONDS, TimeUnit.SECONDS)
                .maximumSize(DEDUP_MAX_SIZE)
                .ticker(ticker)
                .build();
    }

    @Override
    public void check(String clientId, String content) {
        long now = ticker.read();
        String id = normalizeId(clientId);
        String body = normalizeContent(content);

        if (!rateWindow(id).tryAdmit(now)) {
            throw new CheerTalkRateLimitException(CHEER_TALK_RATE_LIMIT_EXCEEDED);
        }
        if (!dedupWindow(id, body).tryAdmit(now)) {
            throw new CheerTalkRateLimitException(CHEER_TALK_DUPLICATE_CONTENT);
        }
    }

    private SlidingWindow rateWindow(String clientId) {
        return rateWindows.get(clientId, k -> new SlidingWindow(RATE_WINDOW_NANOS, RATE_LIMIT));
    }

    private SlidingWindow dedupWindow(String clientId, String content) {
        return dedupWindows.get(new DedupKey(clientId, content),
                k -> new SlidingWindow(DEDUP_WINDOW_NANOS, DEDUP_LIMIT));
    }

    private static String normalizeId(String clientId) {
        return (clientId == null || clientId.isBlank()) ? UNKNOWN_CLIENT : clientId;
    }

    private static String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    private record DedupKey(String clientId, String content) {
    }
}
