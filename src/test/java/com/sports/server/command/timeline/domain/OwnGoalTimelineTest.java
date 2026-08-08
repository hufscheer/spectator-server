package com.sports.server.command.timeline.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OwnGoalTimelineTest {

    private Game game;
    private GameTeam team1;
    private GameTeam team2;

    @BeforeEach
    void setUp() {
        League league = entityBuilder(League.class)
                .set("sportType", SportType.SOCCER)
                .sample();

        game = entityBuilder(Game.class)
                .set("league", league)
                .set("gameTeams", new ArrayList<>())
                .sample();

        team1 = entityBuilder(GameTeam.class)
                .set("id", 1L)
                .set("game", game)
                .set("score", 1)
                .sample();

        team2 = entityBuilder(GameTeam.class)
                .set("id", 2L)
                .set("game", game)
                .set("score", 2)
                .sample();

        game.addGameTeam(team1);
        game.addGameTeam(team2);
    }

    @Nested
    @DisplayName("자책골 타임라인을")
    class CreateTest {

        @Test
        void 자책골_선수가_요청한_팀_소속이면_생성된다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when
            OwnGoalTimeline timeline = OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team1, 1
            );

            // then
            assertAll(
                    () -> assertThat(timeline.getScorer()).isEqualTo(scorer),
                    () -> assertThat(timeline.getType()).isEqualTo(TimelineType.OWN_GOAL)
            );
        }

        @Test
        void 승부차기에서는_생성할_수_없다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when then
            assertThatThrownBy(() -> OwnGoalTimeline.of(
                    game, SoccerQuarter.PENALTY_SHOOTOUT, 10, scorer, team1, 1
            )).isInstanceOf(BadRequestException.class);
        }

        @Test
        void 요청한_팀과_실제_소속_팀이_다르면_생성할_수_없다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when then
            assertThatThrownBy(() -> OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team2, 1
            )).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("자책골 타임라인을 적용하면")
    class ApplyTest {

        @Test
        void team1_선수의_자책골이면_team2가_득점하고_스냅샷이_반영된다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();
            OwnGoalTimeline timeline = OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team1, 1
            );

            // when
            timeline.apply();

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(1),
                    () -> assertThat(team2.getScore()).isEqualTo(3),
                    () -> assertThat(timeline.getSnapshotScore1()).isEqualTo(1),
                    () -> assertThat(timeline.getSnapshotScore2()).isEqualTo(3)
            );
        }
    }

    @Nested
    @DisplayName("자책골 타임라인을 롤백하면")
    class RollbackTest {

        @Test
        void team1_선수의_자책골로_인한_team2의_득점이_취소된다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();
            OwnGoalTimeline timeline = OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team1, 1
            );
            timeline.apply();

            // when
            timeline.rollback();

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(1),
                    () -> assertThat(team2.getScore()).isEqualTo(2)
            );
        }
    }
}
