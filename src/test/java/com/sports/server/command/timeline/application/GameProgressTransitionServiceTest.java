package com.sports.server.command.timeline.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/progress-transition-fixture.sql")
@DisplayName("경기 진행 타임라인 전환 규칙")
class GameProgressTransitionServiceTest extends ServiceTest {

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private MemberRepository memberRepository;

    private Member manager;

    private static final long GAME_PRE_GAME = 1L;
    private static final long GAME_FIRST_HALF_STARTED = 2L;
    private static final long GAME_FIRST_HALF_ENDED = 3L;
    private static final long GAME_SECOND_HALF_STARTED = 4L;
    private static final long GAME_SECOND_HALF_ENDED = 5L;
    private static final long GAME_EXTRA_TIME_STARTED = 6L;
    private static final long GAME_EXTRA_TIME_ENDED = 7L;
    private static final long GAME_PENALTY_SHOOTOUT_STARTED = 8L;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("manager@example.com").orElseThrow();
    }

    private TimelineRequest.RegisterProgress progressRequest(SoccerQuarter quarter, GameProgressType type) {
        return new TimelineRequest.RegisterProgress(0, SportType.SOCCER, quarter.name(), type);
    }

    private void assertValid(long gameId, SoccerQuarter quarter, GameProgressType type) {
        assertThatCode(() -> timelineService.register(manager, gameId, progressRequest(quarter, type)))
                .doesNotThrowAnyException();
    }

    private void assertInvalid(long gameId, SoccerQuarter quarter, GameProgressType type) {
        assertThatThrownBy(() -> timelineService.register(manager, gameId, progressRequest(quarter, type)))
                .isInstanceOf(CustomException.class)
                .hasMessage(TimelineErrorMessage.INVALID_PROGRESS_TRANSITION);
    }

    @Nested
    @DisplayName("경기 시작 전 상태에서는")
    class 경기_시작_전 {

        @Test
        void 경기_시작_요청은_성공한다() {
            assertValid(GAME_PRE_GAME, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 후반전_시작_요청은_실패한다() {
            assertInvalid(GAME_PRE_GAME, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 전반전_종료_요청은_실패한다() {
            assertInvalid(GAME_PRE_GAME, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_END);
        }

        @Test
        void 경기_종료_요청은_실패한다() {
            assertInvalid(GAME_PRE_GAME, SoccerQuarter.FIRST_HALF, GameProgressType.GAME_END);
        }
    }

    @Nested
    @DisplayName("전반전 진행 중 상태에서는")
    class 전반전_진행_중 {

        @Test
        void 전반전_종료_요청은_성공한다() {
            assertValid(GAME_FIRST_HALF_STARTED, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_END);
        }

        @Test
        void 전반전_재시작_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_STARTED, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 후반전_시작_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_STARTED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 경기_종료_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_STARTED, SoccerQuarter.FIRST_HALF, GameProgressType.GAME_END);
        }
    }

    @Nested
    @DisplayName("전반전 종료 후 상태에서는")
    class 전반전_종료_후 {

        @Test
        void 후반전_시작_요청은_성공한다() {
            assertValid(GAME_FIRST_HALF_ENDED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 전반전_재시작_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_ENDED, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 연장전_시작_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_ENDED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }

        @Test
        void 경기_종료_요청은_실패한다() {
            assertInvalid(GAME_FIRST_HALF_ENDED, SoccerQuarter.FIRST_HALF, GameProgressType.GAME_END);
        }
    }

    @Nested
    @DisplayName("후반전 진행 중 상태에서는")
    class 후반전_진행_중 {

        @Test
        void 후반전_종료_요청은_성공한다() {
            assertValid(GAME_SECOND_HALF_STARTED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_END);
        }

        @Test
        void 경기_종료_요청은_성공한다() {
            assertValid(GAME_SECOND_HALF_STARTED, SoccerQuarter.SECOND_HALF, GameProgressType.GAME_END);
        }

        @Test
        void 전반전_시작_요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_STARTED, SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 연장전_시작_요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_STARTED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }

        @Test
        void 후반전_재시작_요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_STARTED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }
    }

    @Nested
    @DisplayName("후반전 종료 후 상태에서는")
    class 후반전_종료_후 {

        @Test
        void 연장전_시작_요청은_성공한다() {
            assertValid(GAME_SECOND_HALF_ENDED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }

        @Test
        void 경기_종료_요청은_성공한다() {
            assertValid(GAME_SECOND_HALF_ENDED, SoccerQuarter.SECOND_HALF, GameProgressType.GAME_END);
        }

        @Test
        void 후반전_재시작_요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_ENDED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 승부차기_시작_요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_ENDED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_START);
        }

        @Test
        void 후반전_종료_재요청은_실패한다() {
            assertInvalid(GAME_SECOND_HALF_ENDED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_END);
        }
    }

    @Nested
    @DisplayName("연장전 진행 중 상태에서는")
    class 연장전_진행_중 {

        @Test
        void 연장전_종료_요청은_성공한다() {
            assertValid(GAME_EXTRA_TIME_STARTED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_END);
        }

        @Test
        void 경기_종료_요청은_성공한다() {
            assertValid(GAME_EXTRA_TIME_STARTED, SoccerQuarter.EXTRA_TIME, GameProgressType.GAME_END);
        }

        @Test
        void 후반전_시작_요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_STARTED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 승부차기_시작_요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_STARTED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_START);
        }

        @Test
        void 연장전_재시작_요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_STARTED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }
    }

    @Nested
    @DisplayName("연장전 종료 후 상태에서는")
    class 연장전_종료_후 {

        @Test
        void 승부차기_시작_요청은_성공한다() {
            assertValid(GAME_EXTRA_TIME_ENDED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_START);
        }

        @Test
        void 경기_종료_요청은_성공한다() {
            assertValid(GAME_EXTRA_TIME_ENDED, SoccerQuarter.EXTRA_TIME, GameProgressType.GAME_END);
        }

        @Test
        void 연장전_재시작_요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_ENDED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }

        @Test
        void 후반전_시작_요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_ENDED, SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START);
        }

        @Test
        void 연장전_종료_재요청은_실패한다() {
            assertInvalid(GAME_EXTRA_TIME_ENDED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_END);
        }
    }

    @Nested
    @DisplayName("승부차기 진행 중 상태에서는")
    class 승부차기_진행_중 {

        @Test
        void 경기_종료_요청은_성공한다() {
            assertValid(GAME_PENALTY_SHOOTOUT_STARTED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.GAME_END);
        }

        @Test
        void 승부차기_종료_요청은_실패한다() {
            assertInvalid(GAME_PENALTY_SHOOTOUT_STARTED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_END);
        }

        @Test
        void 연장전_시작_요청은_실패한다() {
            assertInvalid(GAME_PENALTY_SHOOTOUT_STARTED, SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START);
        }

        @Test
        void 승부차기_재시작_요청은_실패한다() {
            assertInvalid(GAME_PENALTY_SHOOTOUT_STARTED, SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_START);
        }
    }
}
