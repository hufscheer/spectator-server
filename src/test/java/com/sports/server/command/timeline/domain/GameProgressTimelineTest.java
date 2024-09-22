package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.sport.domain.Sport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class GameProgressTimelineTest {
    private Quarter quarter1;
    private Quarter quarter2;
    private Quarter quarter3;
    private Quarter quarter4;
    private Quarter quarter5;

    private Sport sport;

    @BeforeEach
    void setUp() {
        quarter1 = entityBuilder(Quarter.class)
                .set("name", "경기전")
                .set("order", 1)
                .sample();

        quarter2 = entityBuilder(Quarter.class)
                .set("name", "전반전")
                .set("order", 2)
                .sample();

        quarter3 = entityBuilder(Quarter.class)
                .set("name", "후반전")
                .set("order", 3)
                .sample();

        quarter4 = entityBuilder(Quarter.class)
                .set("name", "승부차기")
                .set("order", 4)
                .sample();

        quarter5 = entityBuilder(Quarter.class)
                .set("name", "경기후")
                .set("order", 5)
                .sample();

        sport = entityBuilder(Sport.class)
                .set("name", "축구")
                .set("quarters", List.of(quarter1, quarter2, quarter3, quarter4, quarter5))
                .sample();
    }

    @Nested
    class CreateTest {
        @Test
        void 경기_시작_타임라인을_생성한다() {
            // given
            Game game = entityBuilder(Game.class)
                    .set("teams", new ArrayList<>())
                    .set("gameQuarter", quarter1.getName())
                    .set("state", GameState.SCHEDULED)
                    .set("sport", sport)
                    .sample();

            // when
            GameProgressTimeline timeline = new GameProgressTimeline(
                    game,
                    quarter1,
                    0,
                    GameProgressType.GAME_START
            );

            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(game.getGameQuarter()).isEqualTo(quarter2.getName()),
                    () -> assertThat(game.getState()).isEqualTo(GameState.PLAYING)
            );
        }
    }
}
