package com.sports.server.command.game.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {
    private Game game;

    @BeforeEach
    public void setUp() {
        game = entityBuilder(Game.class)
                .set("teams", new ArrayList<>())
                .sample();

        for (int i = 0; i < 2; i++) {
            game.addTeam(entityBuilder(GameTeam.class)
                    .set("game", game)
                    .set("score", 0)
                    .sample());
        }
    }

    @Nested
    @DisplayName("Game에서")
    class ScoreTest {
        @Test
        void team1이_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam1())
                    .sample();

            // when
            game.score(scorer);

            // then
            assertThat(game.getTeam1().getScore()).isEqualTo(1);
            assertThat(game.getTeam2().getScore()).isEqualTo(0);
        }

        @Test
        void team2가_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam2())
                    .sample();

            // when
            game.score(scorer);

            // then
            assertThat(game.getTeam1().getScore()).isEqualTo(0);
            assertThat(game.getTeam2().getScore()).isEqualTo(1);
        }

        @Test
        void 참여하지_않는_선수는_득점할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.score(scorer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Game에서")
    class CancelScoreTest {

        private LineupPlayer team1Player;
        private LineupPlayer team2Player;

        @BeforeEach
        void setUp() {
            team1Player = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam1())
                    .sample();
            team2Player = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", game.getTeam2())
                    .sample();
        }

        @Test
        void team1의_득점을_취소한다() {
            // given
            game.score(team1Player);

            // when
            game.cancelScore(team1Player);

            // then
            assertThat(game.getTeam1().getScore()).isEqualTo(0);
            assertThat(game.getTeam2().getScore()).isEqualTo(0);
        }

        @Test
        void team2의_득점을_취소한다() {
            // given
            game.score(team2Player);

            // when
            game.cancelScore(team2Player);

            // then
            assertThat(game.getTeam1().getScore()).isEqualTo(0);
            assertThat(game.getTeam2().getScore()).isEqualTo(0);
        }

        @Test
        void 동점_상황에서_한_팀만_점수를_취소한다() {
            // given
            game.score(team1Player);
            game.score(team2Player);

            // when
            game.cancelScore(team1Player);

            // then
            assertThat(game.getTeam1().getScore()).isEqualTo(0);
            assertThat(game.getTeam2().getScore()).isEqualTo(1);
        }

        @Test
        void 참여하지_않는_선수는_득점을_취소할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.cancelScore(scorer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
