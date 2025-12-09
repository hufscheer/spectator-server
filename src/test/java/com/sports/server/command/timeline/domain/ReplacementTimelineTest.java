package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplacementTimelineTest {

    private final Game game = entityBuilder(Game.class)
            .sample();

    private final GameTeam gameTeam = entityBuilder(GameTeam.class)
            .set("id", 1L)
            .set("game", game)
            .sample();

    private final Quarter quarter = entityBuilder(Quarter.class)
            .sample();

    @Nested
    @DisplayName("교체 타임라인은")
    class CreateTest {
        @Test
        void 같은_팀_선수끼리_생성된다() {
            // given
            LineupPlayer originLineupPlayer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam)
                    .sample();

            LineupPlayer replacedLineupPlayer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam)
                    .sample();

            // when
            ReplacementTimeline actual = new ReplacementTimeline(
                    game,
                    quarter,
                    10,
                    originLineupPlayer,
                    replacedLineupPlayer
            );

            // then
            assertThat(actual.getOriginLineupPlayer()).isEqualTo(originLineupPlayer);
            assertThat(actual.getReplacedLineupPlayer()).isEqualTo(replacedLineupPlayer);
        }

        @Test
        void 다른_팀_선수와는_생성할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .set("id", 2L)
                    .set("game", game)
                    .sample();

            LineupPlayer originLineupPlayer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", gameTeam)
                    .sample();

            LineupPlayer replacedLineupPlayer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> new ReplacementTimeline(
                    game,
                    quarter,
                    10,
                    originLineupPlayer,
                    replacedLineupPlayer
            )).isInstanceOf(CustomException.class);
        }
    }
}
