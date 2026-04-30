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

    private static final String IP_A = "1.1.1.1";
    private static final String IP_B = "2.2.2.2";

    private FakeTicker ticker;
    private CheerCountRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CheerCountRateLimiter(ticker);
    }

    @Nested
    @DisplayName("(IP, 게임팀)당 분당 호출수 제한")
    class PerIpGameTeamRate {

        @Test
        void 분당_60회까지는_통과한다() {
            for (int i = 0; i < 60; i++) {
                assertThatCode(() -> rateLimiter.check(IP_A, 1L))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_60회_초과는_429() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(IP_A, 1L);
            }

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L))
                    .isInstanceOf(CheerCountRateLimitException.class);
        }

        @Test
        void 카운터는_1분이_지나면_초기화된다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(IP_A, 1L);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(IP_A, 1L))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_IP라도_게임팀이_다르면_카운터가_분리된다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(IP_A, 1L);
            }

            assertThatCode(() -> rateLimiter.check(IP_A, 2L))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_게임팀이라도_IP가_다르면_카운터가_분리된다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(IP_A, 1L);
            }

            assertThatCode(() -> rateLimiter.check(IP_B, 1L))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 호출수_초과는_사용자_안내_메시지() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(IP_A, 1L);
            }

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L))
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
