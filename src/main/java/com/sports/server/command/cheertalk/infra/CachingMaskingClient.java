package com.sports.server.command.cheertalk.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sports.server.command.cheertalk.application.MaskingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OpenRouter 마스킹 호출 비용을 줄이기 위한 데코레이터.
 * 1) preFilter로 명백히 정상인 메시지는 LLM 스킵
 * 2) LLM이 한 번 처리한 메시지는 결과를 캐싱하여 도배 시 동일 호출 차단
 */
@Component
@Primary
@ConditionalOnProperty(name = "masking.provider", havingValue = "openrouter")
public class CachingMaskingClient implements MaskingClient {

    private final OpenRouterMaskingClient delegate;
    private final MaskingPreFilter preFilter;
    private final Cache<String, String> cache;

    public CachingMaskingClient(
            OpenRouterMaskingClient delegate,
            MaskingPreFilter preFilter,
            @Value("${masking.cache.ttl-minutes:5}") long ttlMinutes,
            @Value("${masking.cache.max-size:10000}") long maxSize
    ) {
        this.delegate = delegate;
        this.preFilter = preFilter;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .build();
    }

    @Override
    public String mask(String content) {
        if (content == null) {
            return null;
        }
        if (preFilter.canSkip(content)) {
            return content;
        }
        String key = content.strip();
        String cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        String masked = delegate.mask(content);
        if (masked != null) {
            cache.put(key, masked);
        }
        return masked;
    }
}
