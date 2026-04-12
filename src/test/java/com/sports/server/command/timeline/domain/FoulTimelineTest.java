package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.Quarter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FoulTimelineTest {

    private final Game game = entityBuilder(Game.class).sample();
    private final GameTeam gameTeam = entityBuilder(GameTeam.class).sample();
    private final Quarter quarter = BasketballQuarter.FIRST_QUARTER;

    @Nested
    @DisplayName("파울 타임라인을")
    class CreateTest {
        @Test
        void 생성한다() {
            // given
            LineupPlayer fouledPlayer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam).sample();

            // when
            FoulTimeline timeline = new FoulTimeline(
                    game,
                    quarter,
                    10,
                    fouledPlayer
            );

            // then
            assertAll(
                    () -> Assertions.assertThat(timeline.getFouledPlayer()).isEqualTo(fouledPlayer),
                    () -> Assertions.assertThat(timeline.getType()).isEqualTo(TimelineType.FOUL)
            );
        }
    }
}
