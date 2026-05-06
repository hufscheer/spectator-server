package com.sports.server.command.cheertalk.infra;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Ticker;
import com.sports.server.command.cheertalk.application.CheerTalkRateLimiter;
import com.sports.server.command.cheertalk.exception.CheerTalkRateLimitException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CaffeineCheerTalkRateLimiterTest {

    private static final String VISITOR_A = "visitor-a";
    private static final String VISITOR_B = "visitor-b";

    private FakeTicker ticker;
    private CheerTalkRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CaffeineCheerTalkRateLimiter(ticker);
    }

    @Nested
    @DisplayName("R1 양적 도배 — 개인당 60초 sliding window 120건")
    class RatePerVisitor {

        @Test
        void 분당_120회까지는_통과한다() {
            for (int i = 0; i < 120; i++) {
                int idx = i;
                assertThatCode(() -> rateLimiter.check(VISITOR_A, "msg-" + idx))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_120회_초과는_429() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A, "msg-overflow"))
                    .isInstanceOf(CheerTalkRateLimitException.class);
        }

        @Test
        void 윈도우_바깥의_요청은_카운트에서_빠진다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A, "msg-" + i);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(VISITOR_A, "msg-after"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 윈도우는_고정이_아니라_미끄러진다() {
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(VISITOR_A, "early-" + i);
            }
            ticker.advance(30, TimeUnit.SECONDS);
            for (int i = 0; i < 60; i++) {
                rateLimiter.check(VISITOR_A, "mid-" + i);
            }
            ticker.advance(31, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(VISITOR_A, "late"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 다른_방문자의_카운터는_분리된다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A, "msg-" + i);
            }

            assertThatCode(() -> rateLimiter.check(VISITOR_B, "msg-other"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 차단된_시도는_카운트에_누적되지_않는다() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A, "msg-" + i);
            }
            for (int i = 0; i < 5; i++) {
                try {
                    rateLimiter.check(VISITOR_A, "blocked-" + i);
                } catch (CheerTalkRateLimitException ignored) {
                }
            }
            ticker.advance(61, TimeUnit.SECONDS);

            for (int i = 0; i < 120; i++) {
                int idx = i;
                assertThatCode(() -> rateLimiter.check(VISITOR_A, "next-" + idx))
                        .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("R2 반복 도배 — 개인당 3초 sliding window 동일 본문 3건까지")
    class DedupPerVisitor {

        @Test
        void 같은_본문이라도_3초_안에_3건까지는_통과한다() {
            for (int i = 0; i < 3; i++) {
                assertThatCode(() -> rateLimiter.check(VISITOR_A, "가즈아"))
                        .doesNotThrowAnyException();
                ticker.advance(500, TimeUnit.MILLISECONDS);
            }
        }

        @Test
        void 같은_본문_4번째는_차단된다() {
            for (int i = 0; i < 3; i++) {
                rateLimiter.check(VISITOR_A, "가즈아");
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A, "가즈아"))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("방금 같은 메시지");
        }

        @Test
        void 첫_요청이_3초를_벗어나면_다시_통과한다() {
            rateLimiter.check(VISITOR_A, "가즈아");
            ticker.advance(1, TimeUnit.SECONDS);
            rateLimiter.check(VISITOR_A, "가즈아");
            ticker.advance(1, TimeUnit.SECONDS);
            rateLimiter.check(VISITOR_A, "가즈아");
            ticker.advance(1100, TimeUnit.MILLISECONDS);

            assertThatCode(() -> rateLimiter.check(VISITOR_A, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 본문이_다르면_별도_카운트된다() {
            for (int i = 0; i < 3; i++) {
                rateLimiter.check(VISITOR_A, "가즈아");
            }

            assertThatCode(() -> rateLimiter.check(VISITOR_A, "파이팅"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 다른_방문자의_본문은_별도_카운트된다() {
            for (int i = 0; i < 3; i++) {
                rateLimiter.check(VISITOR_A, "가즈아");
            }

            assertThatCode(() -> rateLimiter.check(VISITOR_B, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 앞뒤_공백만_다른_본문은_동일하게_본다() {
            for (int i = 0; i < 3; i++) {
                rateLimiter.check(VISITOR_A, "가즈아");
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A, "  가즈아  "))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("방금 같은 메시지");
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 호출수_초과_안내_메시지() {
            for (int i = 0; i < 120; i++) {
                rateLimiter.check(VISITOR_A, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A, "msg-overflow"))
                    .hasMessageContaining("응원톡을 너무 많이");
        }

        @Test
        void 동일_본문_차단_안내_메시지() {
            for (int i = 0; i < 3; i++) {
                rateLimiter.check(VISITOR_A, "가즈아");
            }

            assertThatThrownBy(() -> rateLimiter.check(VISITOR_A, "가즈아"))
                    .hasMessageContaining("방금 같은 메시지");
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
