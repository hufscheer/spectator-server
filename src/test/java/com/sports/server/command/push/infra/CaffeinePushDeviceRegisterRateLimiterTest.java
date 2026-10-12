package com.sports.server.command.push.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.push.application.PushDeviceRegisterRateLimiter;
import com.sports.server.command.push.exception.PushRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaffeinePushDeviceRegisterRateLimiterTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    private FakeTicker ticker;
    private PushDeviceRegisterRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CaffeinePushDeviceRegisterRateLimiter(ticker);
    }

    @Test
    void 십분에_스무_번까지는_통과한다() {
        for (int i = 0; i < 20; i++) {
            assertThatCode(() -> rateLimiter.check(CLIENT_A)).doesNotThrowAnyException();
        }
    }

    @Test
    void 십분에_스무_번을_넘으면_429() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.check(CLIENT_A);
        }

        assertThatThrownBy(() -> rateLimiter.check(CLIENT_A))
                .isInstanceOf(PushRateLimitException.class);
    }

    @Test
    void 윈도우_바깥의_요청은_카운트에서_빠진다() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.check(CLIENT_A);
        }
        ticker.advance(11, TimeUnit.MINUTES);

        assertThatCode(() -> rateLimiter.check(CLIENT_A)).doesNotThrowAnyException();
    }

    @Test
    void 클라이언트마다_따로_센다() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.check(CLIENT_A);
        }

        assertThatCode(() -> rateLimiter.check(CLIENT_B)).doesNotThrowAnyException();
    }

    @Test
    void 식별자가_없으면_하나로_묶어_센다() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.check(null);
        }

        assertThatThrownBy(() -> rateLimiter.check(" "))
                .isInstanceOf(PushRateLimitException.class);
    }

    private static class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(long duration, TimeUnit unit) {
            nanos.addAndGet(unit.toNanos(duration));
        }
    }
}
