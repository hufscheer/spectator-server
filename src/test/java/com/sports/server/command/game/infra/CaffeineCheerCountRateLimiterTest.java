package com.sports.server.command.game.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.game.application.CheerCountRateLimiter;
import com.sports.server.command.game.exception.CheerCountRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CaffeineCheerCountRateLimiterTest {

    private static final String VISITOR_A = "visitor-a";
    private static final String VISITOR_B = "visitor-b";

    private FakeTicker ticker;
    private CheerCountRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CaffeineCheerCountRateLimiter(ticker);
    }

    @Nested
    @DisplayName("R1 양적 제한 — 개인당 60초 sliding window 120건")
    class RatePerVisitor {

        @Test
        void 분당_120회까지는_통과한다() {
            for (int i = 0; i < 120; i++) {
                assertThatCode(() -> rateLimiter.check(VISITOR_A))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_120회_초과는_429() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A);
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A))
                    .isInstanceOf(CheerCountRateLimitException.class);
        }

        @Test
        void 윈도우_바깥의_요청은_카운트에서_빠진다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(VISITOR_A))
                    .doesNotThrowAnyException();
        }

        @Test
        void 윈도우는_고정이_아니라_미끄러진다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(VISITOR_A);
            }
            ticker.advance(30, TimeUnit.SECONDS);
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(VISITOR_A);
            }
            ticker.advance(31, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(VISITOR_A))
                    .doesNotThrowAnyException();
        }

        @Test
        void 다른_방문자의_카운터는_분리된다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A);
            }

            assertThatCode(() -> rateLimiter.check(VISITOR_B))
                    .doesNotThrowAnyException();
        }

        @Test
        void 차단된_시도는_카운트에_누적되지_않는다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A);
            }
            for (int i = 0; i < 5; i++) {
                try {
                    rateLimiter.check(VISITOR_A);
                } catch (CheerCountRateLimitException ignored) {
                }
            }
            ticker.advance(61, TimeUnit.SECONDS);

            for (int i = 0; i < 120; i++) {
                assertThatCode(() -> rateLimiter.check(VISITOR_A))
                        .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 호출수_초과_안내_메시지() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A);
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A))
                    .hasMessageContaining("응원을 너무 많이");
        }
    }

    private static class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(long amount, TimeUnit unit) {
            nanos.addAndGet(unit.toNanos(amount));
        }
    }
}
