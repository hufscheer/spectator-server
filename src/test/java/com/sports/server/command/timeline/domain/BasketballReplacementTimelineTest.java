package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class BasketballReplacementTimelineTest {

    private final Game game = entityBuilder(Game.class).sample();
    private final GameTeam gameTeam = entityBuilder(GameTeam.class).set("id", 1L).set("game", game).sample();
    private final Quarter quarter = BasketballQuarter.FIRST_QUARTER;

    @Nested
    @DisplayName("농구 교체 타임라인은")
    class CreateTest {

        @Test
        void 파울_아웃으로_생성된다() {
            // given
            LineupPlayer origin = entityBuilder(LineupPlayer.class).set("gameTeam", gameTeam).sample();
            LineupPlayer replacement = entityBuilder(LineupPlayer.class).set("gameTeam", gameTeam).sample();

            // when
            BasketballReplacementTimeline timeline = new BasketballReplacementTimeline(
                    game, quarter, 10, origin, replacement, true
            );

            // then
            assertAll(
                    () -> assertThat(timeline.getOriginLineupPlayer()).isEqualTo(origin),
                    () -> assertThat(timeline.getReplacedLineupPlayer()).isEqualTo(replacement),
                    () -> assertThat(timeline.isFoulOut()).isTrue(),
                    () -> assertThat(timeline.getType()).isEqualTo(TimelineType.BASKETBALL_REPLACEMENT)
            );
        }

        @Test
        void 일반_교체로_생성된다() {
            // given
            LineupPlayer origin = entityBuilder(LineupPlayer.class).set("gameTeam", gameTeam).sample();
            LineupPlayer replacement = entityBuilder(LineupPlayer.class).set("gameTeam", gameTeam).sample();

            // when
            BasketballReplacementTimeline timeline = new BasketballReplacementTimeline(
                    game, quarter, 10, origin, replacement, false
            );

            // then
            assertThat(timeline.isFoulOut()).isFalse();
        }

        @Test
        void 다른_팀_선수와는_생성할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class).set("id", 2L).set("game", game).sample();
            LineupPlayer origin = entityBuilder(LineupPlayer.class).set("gameTeam", gameTeam).sample();
            LineupPlayer replacement = entityBuilder(LineupPlayer.class).set("gameTeam", otherTeam).sample();

            // when & then
            assertThatThrownBy(() -> new BasketballReplacementTimeline(
                    game, quarter, 10, origin, replacement, false
            )).isInstanceOf(CustomException.class);
        }
    }
}
