package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.sport.domain.Sport;
import com.sports.server.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class GameProgressTimelineTest {
    private Quarter 경기전;
    private Quarter 전반전;
    private Quarter 후반전;
    private Quarter 승부차기;
    private Quarter 경기후;

    private Sport sport;

    @BeforeEach
    void setUp() {
        경기전 = entityBuilder(Quarter.class)
                .set("name", "경기전")
                .set("order", 1)
                .sample();

        전반전 = entityBuilder(Quarter.class)
                .set("name", "전반전")
                .set("order", 2)
                .sample();

        후반전 = entityBuilder(Quarter.class)
                .set("name", "후반전")
                .set("order", 3)
                .sample();

        승부차기 = entityBuilder(Quarter.class)
                .set("name", "승부차기")
                .set("order", 4)
                .sample();

        경기후 = entityBuilder(Quarter.class)
                .set("name", "경기후")
                .set("order", 5)
                .sample();

        sport = entityBuilder(Sport.class)
                .set("name", "축구")
                .set("quarters", List.of(경기전, 전반전, 후반전, 승부차기, 경기후))
                .sample();
    }

    @Nested
    class ApplyTest {
        private Game game;

        @BeforeEach
        void setUp() {
            game = entityBuilder(Game.class)
                    .set("teams", new ArrayList<>())
                    .set("gameQuarter", 경기전.getName())
                    .set("state", GameState.SCHEDULED)
                    .set("sport", sport)
                    .sample();
        }

        @Test
        void 경기_시작_타임라인을_생성한다() {
            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    경기전,
                    0,
                    GameProgressType.GAME_START
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(전반전.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 전반전_시작_타임라인을_생성한다() {
            // given
            경기_시작_타임라인을_생성한다();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    전반전,
                    0,
                    GameProgressType.QUARTER_START
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(전반전.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 전반전_종료_타임라인을_생성한다() {
            // given
            전반전_시작_타임라인을_생성한다();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    전반전,
                    45,
                    GameProgressType.QUARTER_END
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(전반전.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 후반전_시작_타임라인을_생성한다() {
            // given
            전반전_종료_타임라인을_생성한다();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    후반전,
                    50,
                    GameProgressType.QUARTER_START
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(후반전.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 후반전_종료_타임라인을_생성한다() {
            // given
            후반전_시작_타임라인을_생성한다();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    후반전,
                    90,
                    GameProgressType.QUARTER_END
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(후반전.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }

        @Test
        void 경기_종료_타임라인을_생성한다() {
            // given
            후반전_종료_타임라인을_생성한다();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    후반전,
                    90,
                    GameProgressType.GAME_END
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(경기후.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.FINISHED)
            );
        }

        @Test
        void 쿼터_순서를_오름차순으로만_생성할_수_있다() {
            // given
            후반전_시작_타임라인을_생성한다();

            // when then
            assertThatThrownBy(() -> new GameProgressTimeline(
                    game,
                    전반전,
                    10,
                    GameProgressType.QUARTER_START)
            ).isInstanceOf(CustomException.class)
                    .hasMessage("이전 쿼터로의 진행은 불가능합니다.");
        }
    }
}
