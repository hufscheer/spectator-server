package com.sports.server.command.game.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.common.exception.CustomException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GameTest {
    private Game game;
    private GameTeam team1;
    private GameTeam team2;
    private GameTeam team3;

    @BeforeEach
    public void setUp() {
        game = entityBuilder(Game.class)
                .set("teams", new ArrayList<>())
                .sample();

        team1 = entityBuilder(GameTeam.class)
                .set("game", game)
                .set("score", 0)
                .set("pkScore", 0)
                .sample();

        team2 = entityBuilder(GameTeam.class)
                .set("game", game)
                .set("score", 0)
                .set("pkScore", 0)
                .sample();

        team3 = entityBuilder(GameTeam.class)
                .set("game", game)
                .set("score", 0)
                .set("pkScore", 0)
                .sample();

        game.addGameTeam(team1);
        game.addGameTeam(team2);
    }

    @Nested
    @DisplayName("Game에서")
    class ScoreTest {
        @Test
        void team1이_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when
            game.score(scorer);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(1),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void team2가_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team2)
                    .sample();

            // when
            game.score(scorer);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(1)
            );
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

        @Test
        void team1이_승부차기에서_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when
            game.scoreInPk(scorer);

            // then
            assertAll(
                    () -> assertThat(team1.getPkScore()).isEqualTo(1),
                    () -> assertThat(team2.getPkScore()).isEqualTo(0)
            );
        }

        @Test
        void team2가_승부차기에서_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team2)
                    .sample();

            // when
            game.scoreInPk(scorer);

            // then
            assertAll(
                    () -> assertThat(team1.getPkScore()).isEqualTo(0),
                    () -> assertThat(team2.getPkScore()).isEqualTo(1)
            );
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
                    .set("gameTeam", team1)
                    .sample();
            team2Player = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team2)
                    .sample();
        }

        @Test
        void team1의_득점을_취소한다() {
            // given
            game.score(team1Player);

            // when
            game.cancelScore(team1Player);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void team1의_승부차기_득점을_취소한다() {
            // given
            game.scoreInPk(team1Player);

            // when
            game.cancelPkScore(team1Player);

            // then
            assertAll(
                    () -> assertThat(team1.getPkScore()).isEqualTo(0),
                    () -> assertThat(team2.getPkScore()).isEqualTo(0)
            );
        }

        @Test
        void team2의__승부차기_득점을_취소한다() {
            // given
            game.scoreInPk(team2Player);

            // when
            game.cancelPkScore(team2Player);

            // then
            assertAll(
                    () -> assertThat(team1.getPkScore()).isEqualTo(0),
                    () -> assertThat(team2.getPkScore()).isEqualTo(0)
            );
        }

        @Test
        void team2의_득점을_취소한다() {
            // given
            game.score(team2Player);

            // when
            game.cancelScore(team2Player);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void 동점_상황에서_한_팀만_점수를_취소한다() {
            // given
            game.score(team1Player);
            game.score(team2Player);

            // when
            game.cancelScore(team1Player);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(1)
            );
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

        @Test
        void 참여하지_않는_선수는_승부차기_득점을_취소할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.cancelPkScore(scorer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }


    @Test
    void 주장_상태를_변경할_때_게임에_속하지_않는_게임팀에_대한_요청인_경우_예외를_던진다() {
        // given
        LineupPlayer lineupPlayer = entityBuilder(LineupPlayer.class)
                .set("gameTeam", team3)
                .sample();

        // when & then
        assertThatThrownBy(() -> game.changePlayerToCaptain(lineupPlayer))
                .hasMessage("해당 게임팀은 이 게임에 포함되지 않습니다.")
                .isInstanceOf(CustomException.class);
    }


}
