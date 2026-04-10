package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.query.dto.response.AvailableProgressResponse.ProgressAction;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/progress-transition-fixture.sql")
@DisplayName("경기 진행 가능 액션 조회")
class AvailableProgressQueryServiceTest extends ServiceTest {

    @Autowired
    private TimelineQueryService timelineQueryService;

    private static final long GAME_PRE_GAME = 1L;
    private static final long GAME_FIRST_HALF_STARTED = 2L;
    private static final long GAME_FIRST_HALF_ENDED = 3L;
    private static final long GAME_SECOND_HALF_STARTED = 4L;
    private static final long GAME_SECOND_HALF_ENDED = 5L;
    private static final long GAME_EXTRA_TIME_STARTED = 6L;
    private static final long GAME_EXTRA_TIME_ENDED = 7L;
    private static final long GAME_PENALTY_SHOOTOUT_STARTED = 8L;
    private static final long GAME_FINISHED = 9L;

    private void assertAction(ProgressAction action, SoccerQuarter quarter, GameProgressType type, String displayName) {
        assertAll(
                () -> assertThat(action.quarter()).isEqualTo(quarter.name()),
                () -> assertThat(action.gameProgressType()).isEqualTo(type),
                () -> assertThat(action.displayName()).isEqualTo(displayName)
        );
    }

    @Nested
    @DisplayName("경기 시작 전에는")
    class 경기_시작_전 {

        @Test
        void 경기_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_PRE_GAME).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_START, "전반전 시작");
        }
    }

    @Nested
    @DisplayName("전반전 진행 중에는")
    class 전반전_진행_중 {

        @Test
        void 전반전_종료_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_FIRST_HALF_STARTED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), SoccerQuarter.FIRST_HALF, GameProgressType.QUARTER_END, "전반전 종료");
        }
    }

    @Nested
    @DisplayName("전반전 종료 후에는")
    class 전반전_종료_후 {

        @Test
        void 후반전_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_FIRST_HALF_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_START, "후반전 시작");
        }
    }

    @Nested
    @DisplayName("후반전 진행 중에는")
    class 후반전_진행_중 {

        @Test
        void 후반전_종료와_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_SECOND_HALF_STARTED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), SoccerQuarter.SECOND_HALF, GameProgressType.QUARTER_END, "후반전 종료");
            assertAction(actions.get(1), SoccerQuarter.SECOND_HALF, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("후반전 종료 후에는")
    class 후반전_종료_후 {

        @Test
        void 연장전_시작과_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_SECOND_HALF_ENDED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_START, "연장전 시작");
            assertAction(actions.get(1), SoccerQuarter.SECOND_HALF, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("연장전 진행 중에는")
    class 연장전_진행_중 {

        @Test
        void 연장전_종료와_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_EXTRA_TIME_STARTED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), SoccerQuarter.EXTRA_TIME, GameProgressType.QUARTER_END, "연장전 종료");
            assertAction(actions.get(1), SoccerQuarter.EXTRA_TIME, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("연장전 종료 후에는")
    class 연장전_종료_후 {

        @Test
        void 승부차기_시작과_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_EXTRA_TIME_ENDED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.QUARTER_START, "승부차기 시작");
            assertAction(actions.get(1), SoccerQuarter.EXTRA_TIME, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("승부차기 진행 중에는")
    class 승부차기_진행_중 {

        @Test
        void 경기_종료_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_PENALTY_SHOOTOUT_STARTED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), SoccerQuarter.PENALTY_SHOOTOUT, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("경기 종료 후에는")
    class 경기_종료_후 {

        @Test
        void 가능한_액션이_없다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(GAME_FINISHED).availableActions();

            assertThat(actions).isEmpty();
        }
    }
}
