package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class ScoreTimelineTest {
    private final Game game = entityBuilder(Game.class)
            .set("teams", new ArrayList<>())
            .sample();

    private final Quarter quarter = entityBuilder(Quarter.class)
            .sample();

    @BeforeEach
    void setUp() {
        game.addTeam(entityBuilder(GameTeam.class)
                .set("game", game)
                .set("id", 1L)
                .set("score", 1)
                .sample());

        game.addTeam(entityBuilder(GameTeam.class)
                .set("game", game)
                .set("id", 2L)
                .set("score", 2)
                .sample());
    }

    @Nested
    @DisplayName("점수 타임라인을")
    class CreateTest {
        @Test
        void team1_득점으로_생성한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam1())
                    .sample();

            // when
            ScoreTimeline timeline = ScoreTimeline.score(
                    game,
                    quarter,
                    10,
                    scorer
            );

            timeline.apply();

            // then
            assertThat(timeline.getScorer()).isEqualTo(scorer);
            assertThat(timeline.getSnapshotScore1()).isEqualTo(2);
            assertThat(timeline.getSnapshotScore2()).isEqualTo(2);
        }

        @Test
        void team2_득점으로_생성한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam2())
                    .sample();

            // when
            ScoreTimeline timeline = ScoreTimeline.score(
                    game,
                    quarter,
                    10,
                    scorer
            );

            timeline.apply();

            // then
            assertThat(timeline.getScorer()).isEqualTo(scorer);
            assertThat(timeline.getSnapshotScore1()).isEqualTo(1);
            assertThat(timeline.getSnapshotScore2()).isEqualTo(3);
        }
    }
}
