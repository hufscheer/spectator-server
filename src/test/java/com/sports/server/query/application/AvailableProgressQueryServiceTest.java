package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.query.dto.response.AvailableProgressResponse.ProgressAction;
import com.sports.server.query.dto.response.QuarterScoreResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private void assertAction(ProgressAction action, BasketballQuarter quarter, GameProgressType type, String displayName) {
        assertAll(
                () -> assertThat(action.quarter()).isEqualTo(quarter.name()),
                () -> assertThat(action.gameProgressType()).isEqualTo(type),
                () -> assertThat(action.displayName()).isEqualTo(displayName)
        );
    }

    private static final long BASKETBALL_GAME_PRE_GAME = 10L;
    private static final long BASKETBALL_GAME_1Q_STARTED = 11L;
    private static final long BASKETBALL_GAME_1Q_ENDED = 12L;
    private static final long BASKETBALL_GAME_2Q_STARTED = 13L;
    private static final long BASKETBALL_GAME_2Q_ENDED = 14L;
    private static final long BASKETBALL_GAME_3Q_STARTED = 15L;
    private static final long BASKETBALL_GAME_3Q_ENDED = 16L;
    private static final long BASKETBALL_GAME_4Q_STARTED = 17L;
    private static final long BASKETBALL_GAME_4Q_ENDED = 18L;
    private static final long BASKETBALL_GAME_OT_STARTED = 19L;
    private static final long BASKETBALL_GAME_OT_ENDED = 20L;
    private static final long BASKETBALL_GAME_FINISHED = 21L;

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

    @Nested
    @DisplayName("[농구] 경기 시작 전에는")
    class 농구_경기_시작_전 {

        @Test
        void 경기_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_PRE_GAME).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.FIRST_QUARTER, GameProgressType.QUARTER_START, "1쿼터 시작");
        }
    }

    @Nested
    @DisplayName("[농구] 1Q 진행 중에는")
    class 농구_1Q_진행_중 {

        @Test
        void Q1_종료_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_1Q_STARTED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.FIRST_QUARTER, GameProgressType.QUARTER_END, "1쿼터 종료");
        }
    }

    @Nested
    @DisplayName("[농구] 1Q 종료 후에는")
    class 농구_1Q_종료_후 {

        @Test
        void Q2_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_1Q_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.SECOND_QUARTER, GameProgressType.QUARTER_START, "2쿼터 시작");
        }
    }

    @Nested
    @DisplayName("[농구] 2Q 진행 중에는")
    class 농구_2Q_진행_중 {

        @Test
        void Q2_종료_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_2Q_STARTED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.SECOND_QUARTER, GameProgressType.QUARTER_END, "2쿼터 종료");
        }
    }

    @Nested
    @DisplayName("[농구] 2Q 종료 후에는")
    class 농구_2Q_종료_후 {

        @Test
        void Q3_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_2Q_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.THIRD_QUARTER, GameProgressType.QUARTER_START, "3쿼터 시작");
        }
    }

    @Nested
    @DisplayName("[농구] 3Q 진행 중에는")
    class 농구_3Q_진행_중 {

        @Test
        void Q3_종료_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_3Q_STARTED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.THIRD_QUARTER, GameProgressType.QUARTER_END, "3쿼터 종료");
        }
    }

    @Nested
    @DisplayName("[농구] 3Q 종료 후에는")
    class 농구_3Q_종료_후 {

        @Test
        void Q4_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_3Q_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.FOURTH_QUARTER, GameProgressType.QUARTER_START, "4쿼터 시작");
        }
    }

    @Nested
    @DisplayName("[농구] 4Q 진행 중에는")
    class 농구_4Q_진행_중 {

        @Test
        void Q4_종료와_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_4Q_STARTED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), BasketballQuarter.FOURTH_QUARTER, GameProgressType.QUARTER_END, "4쿼터 종료");
            assertAction(actions.get(1), BasketballQuarter.FOURTH_QUARTER, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("[농구] 4Q 종료 후에는")
    class 농구_4Q_종료_후 {

        @Test
        void OT_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_4Q_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.OVERTIME, GameProgressType.QUARTER_START, "연장전 시작");
        }
    }

    @Nested
    @DisplayName("[농구] OT 진행 중에는")
    class 농구_OT_진행_중 {

        @Test
        void OT_종료와_경기_종료_액션을_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_OT_STARTED).availableActions();

            assertThat(actions).hasSize(2);
            assertAction(actions.get(0), BasketballQuarter.OVERTIME, GameProgressType.QUARTER_END, "연장전 종료");
            assertAction(actions.get(1), BasketballQuarter.OVERTIME, GameProgressType.GAME_END, "경기 종료");
        }
    }

    @Nested
    @DisplayName("[농구] OT 종료 후에는")
    class 농구_OT_종료_후 {

        @Test
        void OT_시작_액션_하나만_반환한다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_OT_ENDED).availableActions();

            assertThat(actions).hasSize(1);
            assertAction(actions.get(0), BasketballQuarter.OVERTIME, GameProgressType.QUARTER_START, "연장전 시작");
        }
    }

    @Nested
    @DisplayName("[농구] 경기 종료 후에는")
    class 농구_경기_종료_후 {

        @Test
        void 가능한_액션이_없다() {
            List<ProgressAction> actions = timelineQueryService.getAvailableProgress(BASKETBALL_GAME_FINISHED).availableActions();

            assertThat(actions).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿼터별 득점 조회")
    @Sql(scripts = "/timeline-fixture.sql")
    class 쿼터별_득점_조회 {

        private static final long BASKETBALL_GAME_ID = 5L;
        private static final long TEAM_A_GAME_TEAM_ID = 7L;
        private static final long TEAM_B_GAME_TEAM_ID = 8L;

        @Test
        void 완료된_쿼터별_득점을_조회한다() {
            List<QuarterScoreResponse> responses = timelineQueryService.getQuarterScores(BASKETBALL_GAME_ID);

            assertThat(responses).hasSize(1);

            QuarterScoreResponse firstQuarterScore = responses.get(0);
            assertAll(
                    () -> assertThat(firstQuarterScore.quarter()).isEqualTo(BasketballQuarter.FIRST_QUARTER.name()),
                    () -> assertThat(firstQuarterScore.displayName()).isEqualTo("1쿼터"),
                    () -> assertThat(firstQuarterScore.scores()).hasSize(2)
            );

            Map<Long, Integer> scoreMap = firstQuarterScore.scores().stream()
                    .collect(Collectors.toMap(QuarterScoreResponse.TeamScore::gameTeamId, QuarterScoreResponse.TeamScore::score));

            assertAll(
                    () -> assertThat(scoreMap.get(TEAM_A_GAME_TEAM_ID)).isEqualTo(3),
                    () -> assertThat(scoreMap.get(TEAM_B_GAME_TEAM_ID)).isEqualTo(2)
            );
        }

        @Test
        void 진행_중인_쿼터는_포함하지_않는다() {
            List<QuarterScoreResponse> responses = timelineQueryService.getQuarterScores(BASKETBALL_GAME_ID);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).quarter()).isEqualTo(BasketballQuarter.FIRST_QUARTER.name());
        }

        @Test
        void QUARTER_END가_없으면_빈_리스트를_반환한다() {
            long soccerGameId = 1L;
            List<QuarterScoreResponse> responses = timelineQueryService.getQuarterScores(soccerGameId);

            assertThat(responses).isEmpty();
        }
    }
}
