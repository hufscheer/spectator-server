package com.sports.server.command.game.infra;

import static com.sports.server.command.game.exception.GameErrorMessages.CHEER_COUNT_RATE_LIMIT_EXCEEDED;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.game.exception.CheerCountRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * (clientIp, gameTeamId) 단위 응원 카운트(/games/{id}/cheer) 호출 한도를 방어한다.
 * 분당 한도 초과 시 429.
 * 응원톡과 달리 정수 batch 호출이라 dedup은 두지 않는다.
 */
@Component
public class CheerCountRateLimiter {

    private static final int MAX_PER_MINUTE_PER_IP_GAME_TEAM = 60;
    private static final long COUNTER_TTL_MINUTES = 1L;
    private static final long COUNTER_MAX_SIZE = 200_000L;

    private final Cache<CounterKey, AtomicInteger> perIpGameTeamCounter;

    public CheerCountRateLimiter() {
        this(Ticker.systemTicker());
    }

    CheerCountRateLimiter(Ticker ticker) {
        this.perIpGameTeamCounter = Caffeine.newBuilder()
                .expireAfterWrite(COUNTER_TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(COUNTER_MAX_SIZE)
                .ticker(ticker)
                .build();
    }

    public void check(String clientIp, Long gameTeamId) {
        if (gameTeamId == null) {
            return;
        }
        String ip = clientIp == null ? "unknown" : clientIp;
        CounterKey key = new CounterKey(ip, gameTeamId);
        AtomicInteger counter = perIpGameTeamCounter.get(key, k -> new AtomicInteger(0));
        if (counter.incrementAndGet() > MAX_PER_MINUTE_PER_IP_GAME_TEAM) {
            throw new CheerCountRateLimitException(CHEER_COUNT_RATE_LIMIT_EXCEEDED);
        }
    }

    private record CounterKey(String clientIp, Long gameTeamId) {
    }
}
