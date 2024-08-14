package com.sports.server.command.game.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

    @Nested
    @DisplayName("Game에서 주장으로 등록할 때")
    class changeCaptainStatusTest {
        private Game game;
        private LineupPlayer team1FirstPlayer;
        private LineupPlayer team1SecondPlayer;
        private LineupPlayer team2FirstPlayer;
        private LineupPlayer team2SecondPlayer;

        @BeforeEach
        void setUp() {
            game = entityBuilder(Game.class)
                    .set("teams", new ArrayList<>())
                    .sample();

            // 주장이 이미 존재하는 팀
            team1FirstPlayer = entityBuilder(LineupPlayer.class)
                    .set("isCaptain", true)
                    .sample();
            team1SecondPlayer = entityBuilder(LineupPlayer.class)
                    .set("isCaptain", false)
                    .sample();

            // 주장이 없는 팀
            team2FirstPlayer = entityBuilder(LineupPlayer.class)
                    .set("isCaptain", false)
                    .sample();
            team2SecondPlayer = entityBuilder(LineupPlayer.class)
                    .set("isCaptain", false)
                    .sample();

            GameTeam teamWithCaptain = entityBuilder(GameTeam.class)
                    .set("game", game)
                    .set("score", 0)
                    .set("lineupPlayers", List.of(team1FirstPlayer, team1SecondPlayer))
                    .sample();

            GameTeam teamWithoutCaptain = entityBuilder(GameTeam.class)
                    .set("game", game)
                    .set("score", 0)
                    .set("lineupPlayers", List.of(team2FirstPlayer, team2SecondPlayer))
                    .sample();

            game.addTeam(teamWithCaptain);
            game.addTeam(teamWithoutCaptain);
        }

        @Test
        void 주장이_없는_팀은_주장_등록을_할_수_있다() {
            // when
            game.changeCaptainStatus(team2FirstPlayer);

            // then
            assertThat(team2FirstPlayer.isCaptain()).isEqualTo(true);
        }

        @Test
        void 주장이_있는_팀은_주장_등록을_할_수_없다() {
            // when & then
            assertThatThrownBy(
                    () -> game.changeCaptainStatus(team1SecondPlayer))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주장은 두 명 이상 등록할 수 없습니다.");
        }

        @Test
        void 주장인_선수는_주장이_아니도록_변경한다() {
            // when
            game.changeCaptainStatus(team1FirstPlayer);

            // then
            assertThat(team1FirstPlayer.isCaptain()).isEqualTo(false);
        }


    }
}
