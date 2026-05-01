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

    private static final String IP_A = "1.1.1.1";
    private static final String IP_B = "2.2.2.2";

    private FakeTicker ticker;
    private CheerTalkRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        ticker = new FakeTicker();
        rateLimiter = new CaffeineCheerTalkRateLimiter(ticker);
    }

    @Nested
    @DisplayName("동일 본문 중복 차단")
    class Dedup {

        @Test
        void 같은_IP_같은_게임팀_같은_본문이_5초_이내_재전송되면_예외() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "가즈아"))
                    .isInstanceOf(CheerTalkRateLimitException.class);
        }

        @Test
        void 같은_본문이라도_5초가_지나면_허용() {
            rateLimiter.check(IP_A, 1L, "가즈아");
            ticker.advance(6, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(IP_A, 1L, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_게임팀이라도_본문이_다르면_통과() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatCode(() -> rateLimiter.check(IP_A, 1L, "파이팅"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_본문이라도_게임팀이_다르면_통과() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatCode(() -> rateLimiter.check(IP_A, 2L, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_본문이라도_IP가_다르면_통과() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatCode(() -> rateLimiter.check(IP_B, 1L, "가즈아"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 앞뒤_공백만_다른_본문도_중복으로_본다() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "  가즈아  "))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("동일한 응원톡");
        }
    }

    @Nested
    @DisplayName("(IP, 게임팀)당 분당 호출수 제한")
    class PerIpGameTeamRate {

        @Test
        void 분당_30회까지는_통과한다() {
            for (int i = 0; i < 30; i++) {
                int idx = i;
                assertThatCode(() -> rateLimiter.check(IP_A, 1L, "msg-" + idx))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void 분당_30회_초과는_429() {
            for (int i = 0; i < 30; i++) {
                rateLimiter.check(IP_A, 1L, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "msg-31"))
                    .isInstanceOf(CheerTalkRateLimitException.class);
        }

        @Test
        void 카운터는_1분이_지나면_초기화된다() {
            for (int i = 0; i < 30; i++) {
                rateLimiter.check(IP_A, 1L, "msg-" + i);
            }
            ticker.advance(61, TimeUnit.SECONDS);

            assertThatCode(() -> rateLimiter.check(IP_A, 1L, "msg-after"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_IP라도_게임팀이_다르면_카운터가_분리된다() {
            for (int i = 0; i < 30; i++) {
                rateLimiter.check(IP_A, 1L, "msg-" + i);
            }

            assertThatCode(() -> rateLimiter.check(IP_A, 2L, "msg-other-team"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 같은_게임팀이라도_IP가_다르면_카운터가_분리된다() {
            for (int i = 0; i < 30; i++) {
                rateLimiter.check(IP_A, 1L, "msg-" + i);
            }

            assertThatCode(() -> rateLimiter.check(IP_B, 1L, "msg-other-ip"))
                    .doesNotThrowAnyException();
        }

        @Test
        void 중복_본문이라도_분당_한도에_누적된다() {
            rateLimiter.check(IP_A, 1L, "가즈아");
            for (int i = 0; i < 29; i++) {
                try {
                    rateLimiter.check(IP_A, 1L, "가즈아");
                } catch (CheerTalkRateLimitException ignored) {
                }
            }

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "다른본문"))
                    .isInstanceOf(CheerTalkRateLimitException.class)
                    .hasMessageContaining("전송 가능 횟수를 초과했습니다");
        }
    }

    @Nested
    @DisplayName("예외 메시지")
    class Messages {

        @Test
        void 중복_본문은_사용자_안내_메시지() {
            rateLimiter.check(IP_A, 1L, "가즈아");

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "가즈아"))
                    .hasMessageContaining("동일한 응원톡");
        }

        @Test
        void 호출수_초과는_사용자_안내_메시지() {
            for (int i = 0; i < 30; i++) {
                rateLimiter.check(IP_A, 1L, "msg-" + i);
            }

            assertThatThrownBy(() -> rateLimiter.check(IP_A, 1L, "msg-overflow"))
                    .hasMessageContaining("전송 가능 횟수를 초과했습니다");
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
