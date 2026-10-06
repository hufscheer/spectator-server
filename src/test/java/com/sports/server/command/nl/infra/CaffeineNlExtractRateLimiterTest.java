package com.sports.server.command.nl.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.nl.exception.NlRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CaffeineNlExtractRateLimiterTest {

    private final AtomicLong now = new AtomicLong(TimeUnit.HOURS.toNanos(1));
    private final CaffeineNlExtractRateLimiter limiter = new CaffeineNlExtractRateLimiter(now::get);

    @Test
    void 십분에_열번까지만_허용하고_열한번째는_429() {
        for (int i = 0; i < 10; i++) {
            limiter.check(1L);
        }
        assertThatThrownBy(() -> limiter.check(1L)).isInstanceOf(NlRateLimitException.class);
    }

    @Test
    void 회원별로_따로_센다() {
        for (int i = 0; i < 10; i++) {
            limiter.check(1L);
        }
        assertThatCode(() -> limiter.check(2L)).doesNotThrowAnyException();
    }

    @Test
    void 창이_지나면_다시_허용한다() {
        for (int i = 0; i < 10; i++) {
            limiter.check(1L);
        }
        now.addAndGet(TimeUnit.MINUTES.toNanos(10) + 1);
        assertThatCode(() -> limiter.check(1L)).doesNotThrowAnyException();
    }
}
