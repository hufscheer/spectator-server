package com.sports.server.command.timeline.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.common.exception.CustomException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GameProgressTimelineTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = entityBuilder(Game.class)
                .set("teams", new ArrayList<>())
                .set("gameQuarter", Quarter.PRE_GAME.getName())
                .set("state", GameState.SCHEDULED)
                .set("is_pk_taken", false)
                .sample();
    }

    @Nested
    class ApplyTest {

        @Test
        void 전반전_시작_타임라인을_생성한다() {
            // when
            GameProgressTimeline timeline = 전반전_시작_타임라인_생성(game);

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.FIRST_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 전반전_종료_타임라인을_생성한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();

            // when
            GameProgressTimeline timeline = 전반전_종료_타임라인_생성(game);

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.FIRST_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 후반전_시작_타임라인을_생성한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();
            전반전_종료_타임라인_생성(game).apply();

            // when
            GameProgressTimeline timeline = 후반전_시작_타임라인_생성(game);

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.SECOND_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 후반전_종료_타임라인을_생성한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();
            전반전_종료_타임라인_생성(game).apply();
            후반전_시작_타임라인_생성(game).apply();

            // when
            GameProgressTimeline timeline = 후반전_종료_타임라인_생성(game);

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.SECOND_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 경기_종료_타임라인을_생성한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();
            전반전_종료_타임라인_생성(game).apply();
            후반전_시작_타임라인_생성(game).apply();

            // when
            GameProgressTimeline timeline = 경기_종료_타임라인_생성(game);

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.POST_GAME.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.FINISHED)
            );
        }

        @Test
        void 쿼터_순서를_오름차순으로만_생성할_수_있다() {
            // given
            후반전_시작_타임라인_생성(game).apply();

            // when then
            assertThatThrownBy(() -> new GameProgressTimeline(
                    game,
                    Quarter.FIRST_HALF,
                    10,
                    GameProgressType.QUARTER_START)
            ).isInstanceOf(CustomException.class)
                    .hasMessage("이전 쿼터로의 진행은 불가능합니다.");
        }

        @Test
        void 승부차기_시작_타임라인을_등록하는_경우_game의_승부차기_진행_여부_필드가_true가_된다() {
            // given
            GameProgressTimeline timeline = 승부차기_시작_타임라인_생성(game);

            // when
            timeline.apply();

            // then
            assertThat(game.getIsPkTaken()).isEqualTo(true);
        }
    }

    @Nested
    class RollbackTest {

        @Test
        void 전반전_시작_타임라인을_롤백한다() {
            // given
            GameProgressTimeline timeline = 전반전_시작_타임라인_생성(game);
            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.PRE_GAME.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.SCHEDULED)
            );
        }

        @Test
        void 전반전_종료_타임라인을_롤백한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();

            GameProgressTimeline timeline = 전반전_종료_타임라인_생성(game);

            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.FIRST_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 후반전_시작_타임라인을_롤백한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();
            전반전_종료_타임라인_생성(game).apply();

            GameProgressTimeline timeline = 후반전_시작_타임라인_생성(game);
            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.FIRST_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 승부차기_시작_타임라인을_롤백하는_경우_game의_승부차기_진행_여부_필드가_false가_된다() {
            // given
            GameProgressTimeline timeline = 승부차기_시작_타임라인_생성(game);
            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertThat(game.getIsPkTaken()).isEqualTo(false);
        }

        @Test
        void 경기_종료_타임라인을_롤백한다() {
            // given
            전반전_시작_타임라인_생성(game).apply();
            전반전_종료_타임라인_생성(game).apply();
            후반전_시작_타임라인_생성(game).apply();
            후반전_종료_타임라인_생성(game).apply();

            GameProgressTimeline timeline = 경기_종료_타임라인_생성(game);

            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(Quarter.SECOND_HALF.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }
    }

    private GameProgressTimeline 전반전_시작_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.FIRST_HALF,
                0,
                GameProgressType.QUARTER_START
        );
    }

    private GameProgressTimeline 전반전_종료_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.FIRST_HALF,
                45,
                GameProgressType.QUARTER_END
        );
    }

    private GameProgressTimeline 후반전_시작_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.SECOND_HALF,
                50,
                GameProgressType.QUARTER_START
        );
    }

    private GameProgressTimeline 후반전_종료_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.SECOND_HALF,
                50,
                GameProgressType.QUARTER_END
        );
    }

    private GameProgressTimeline 승부차기_시작_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.PENALTY_SHOOTOUT,
                50,
                GameProgressType.QUARTER_START
        );
    }

    private GameProgressTimeline 경기_종료_타임라인_생성(Game game) {
        return new GameProgressTimeline(
                game,
                Quarter.POST_GAME,
                50,
                GameProgressType.GAME_END
        );
    }
}