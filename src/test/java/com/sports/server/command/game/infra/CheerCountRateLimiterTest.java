package com.sports.server.command.game.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.game.exception.CheerCountRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CheerCountRateLimiterTest {

    private FakeTicker ticker;
    private CheerCountRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CheerCountRateLimiter(ticker);
    }

    @Nested
    @DisplayName("게임팀당 분당 호출수 제한")
    class PerGameTeamRate {

        @Test
        void 분당_60회까지는_통과한다() {
            for (int i = 0; i < 60; i++) {
                assertThatCode(() -> rateLimiter.check(1L))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_60회_초과는_429() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(1L);
            }

            assertThatThrownBy(() -> rateLimiter.check(1L))
                    .isInstanceOf(CheerCountRateLimitException.class);
        }

        @Test
        void 카운터는_1분이_지나면_초기화된다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(1L);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(1L))
                    .doesNotThrowAnyException();
        }

        @Test
        void 게임팀별로_카운터가_분리된다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(1L);
            }

            assertThatCode(() -> rateLimiter.check(2L))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 호출수_초과는_사용자_안내_메시지() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(1L);
            }

            assertThatThrownBy(() -> rateLimiter.check(1L))
                    .hasMessageContaining("초과했습니다");
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
