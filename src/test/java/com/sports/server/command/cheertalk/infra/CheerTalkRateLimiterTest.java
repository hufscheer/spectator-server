package com.sports.server.command.cheertalk.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.cheertalk.exception.CheerTalkRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CheerTalkRateLimiterTest {

    private FakeTicker ticker;
    private CheerTalkRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CheerTalkRateLimiter(ticker);
    }

    @Nested
    @DisplayName("동일 본문 중복 차단")
    class Dedup {

        @Test
        void 같은_게임팀_같은_본문이_5초_이내_재전송되면_예외() {
            rateLimiter.check(1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(1L, "가즈아"))
                    .isInstanceOf(CheerTalkRateLimitException.class);
        }

        @Test
        void 같은_본문이라도_5초가_지나면_허용() {
            rateLimiter.check(1L, "가즈아");
            ticker.advance(6, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(1L, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_게임팀이라도_본문이_다르면_통과() {
            rateLimiter.check(1L, "가즈아");

            assertThatCode(() -> rateLimiter.check(1L, "파이팅"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_본문이라도_게임팀이_다르면_통과() {
            rateLimiter.check(1L, "가즈아");

            assertThatCode(() -> rateLimiter.check(2L, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 앞뒤_공백만_다른_본문도_중복으로_본다() {
            rateLimiter.check(1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(1L, "  가즈아  "))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("동일한 응원톡");
        }
    }

    @Nested
    @DisplayName("게임팀당 분당 호출수 제한")
    class PerGameTeamRate {

        @Test
        void 분당_120회까지는_통과한다() {
            for (int i = 0; i < 120; i++) {
                int idx = i;
                assertThatCode(() -> rateLimiter.check(1L, "msg-" + idx))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_120회_초과는_429() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(1L, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(1L, "msg-121"))
                    .isInstanceOf(CheerTalkRateLimitException.class);
        }

        @Test
        void 카운터는_1분이_지나면_초기화된다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(1L, "msg-" + i);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(1L, "msg-after"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 게임팀별로_카운터가_분리된다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(1L, "msg-" + i);
            }

            assertThatCode(() -> rateLimiter.check(2L, "msg-other-team"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 중복_본문이라도_분당_한도에_누적된다() {
            rateLimiter.check(1L, "가즈아");
            for (int i = 0; i < 119; i++) {
                try {
                    rateLimiter.check(1L, "가즈아");
                } catch (CheerTalkRateLimitException ignored) {
                }
            }

            assertThatThrownBy(() -> rateLimiter.check(1L, "다른본문"))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("너무 빠릅니다");
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 중복_본문은_사용자_안내_메시지() {
            rateLimiter.check(1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(1L, "가즈아"))
                    .hasMessageContaining("동일한 응원톡");
        }

        @Test
        void 호출수_초과는_사용자_안내_메시지() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(1L, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(1L, "msg-overflow"))
                    .hasMessageContaining("너무 빠릅니다");
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
