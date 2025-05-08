package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;

public class WarningCardTimelineTest {

    private final Game game = entityBuilder(Game.class).sample();
    private final GameTeam gameTeam = entityBuilder(GameTeam.class).sample();
    private final Quarter quarter = entityBuilder(Quarter.class).sample();

    @Nested
    @DisplayName("경고 카드 타임라인을")
    class CreateTest{
        @Test
        void 옐로카드로_생성한다(){
            //given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam).sample();

            //when
            WarningCardTimeline timeline = new WarningCardTimeline(
                    game,
                    quarter,
                    10,
                    scorer,
                    WarningCardType.YELLOW
            );

            //then
            assertAll(
                    () -> Assertions.assertThat(timeline.getScorer()).isEqualTo(scorer),
                    () -> Assertions.assertThat(timeline.getWarningCardType()).isEqualTo(WarningCardType.YELLOW)
            );
        }

        @Test
        void 레드카드로_생성한다(){
            //given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam).sample();

            //when
            WarningCardTimeline timeline = new WarningCardTimeline(
                    game,
                    quarter,
                    10,
                    scorer,
                    WarningCardType.RED
            );

            //then
            assertAll(
                    () -> Assertions.assertThat(timeline.getScorer()).isEqualTo(scorer),
                    () -> Assertions.assertThat(timeline.getWarningCardType()).isEqualTo(WarningCardType.RED)
            );
        }
    }
}
