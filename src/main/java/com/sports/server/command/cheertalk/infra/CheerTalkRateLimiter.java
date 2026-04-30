package com.sports.server.command.cheertalk.infra;

import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_DUPLICATE_CONTENT;
import static com.sports.server.command.cheertalk.exception.CheerTalkErrorMessages.CHEER_TALK_RATE_LIMIT_EXCEEDED;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.cheertalk.exception.CheerTalkRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * (clientIp, gameTeamId) 단위 응원톡 호출 한도와 짧은 시간 동일 본문 중복을 방어한다.
 * - 분당 한도 초과: 429
 * - 5초 내 같은 (clientIp, gameTeamId, content) 재전송: 429
 */
@Component
public class CheerTalkRateLimiter {

    private static final int MAX_PER_MINUTE_PER_IP_GAME_TEAM = 30;
    private static final long COUNTER_TTL_MINUTES = 1L;
    private static final long DEDUP_TTL_SECONDS = 5L;
    private static final long COUNTER_MAX_SIZE = 200_000L;
    private static final long DEDUP_MAX_SIZE = 500_000L;

    private final Cache<CounterKey, AtomicInteger> perIpGameTeamCounter;
    private final Cache<DedupKey, Boolean> recentContent;

    public CheerTalkRateLimiter() {
        this(Ticker.systemTicker());
    }

    CheerTalkRateLimiter(Ticker ticker) {
        this.perIpGameTeamCounter = Caffeine.newBuilder()
                .expireAfterWrite(COUNTER_TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(COUNTER_MAX_SIZE)
                .ticker(ticker)
                .build();
        this.recentContent = Caffeine.newBuilder()
                .expireAfterWrite(DEDUP_TTL_SECONDS, TimeUnit.SECONDS)
                .maximumSize(DEDUP_MAX_SIZE)
                .ticker(ticker)
                .build();
    }

    public void check(String clientIp, Long gameTeamId, String content) {
        String ip = clientIp == null ? "unknown" : clientIp;
        CounterKey counterKey = new CounterKey(ip, gameTeamId);
        AtomicInteger counter = perIpGameTeamCounter.get(counterKey, k -> new AtomicInteger(0));
        if (counter.incrementAndGet() > MAX_PER_MINUTE_PER_IP_GAME_TEAM) {
            throw new CheerTalkRateLimitException(CHEER_TALK_RATE_LIMIT_EXCEEDED);
        }

        DedupKey key = new DedupKey(ip, gameTeamId, content == null ? "" : content.trim());
        if (recentContent.asMap().putIfAbsent(key, Boolean.TRUE) != null) {
            throw new CheerTalkRateLimitException(CHEER_TALK_DUPLICATE_CONTENT);
        }
    }

    private record CounterKey(String clientIp, Long gameTeamId) {
    }

    private record DedupKey(String clientIp, Long gameTeamId, String content) {
    }
}
