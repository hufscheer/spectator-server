package com.sports.server.command.game.domain;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.CustomException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GameTest {
    private Game game;
    private Game game2;
    private GameTeam team1;
    private GameTeam team2;

    @BeforeEach
    public void setUp() {
        League soccerLeague = entityBuilder(League.class)
                .set("sportType", SportType.SOCCER)
                .sample();

        game = entityBuilder(Game.class)
                .set("id", 1L)
                .set("league", soccerLeague)
                .set("gameTeams", new ArrayList<>())
                .sample();

        game2 = entityBuilder(Game.class)
                .set("id", 2L)
                .set("league", soccerLeague)
                .set("gameTeams", new ArrayList<>())
                .sample();

        team1 = entityBuilder(GameTeam.class)
                .set("id", 1L)
                .set("game", game)
                .set("score", 0)
                .set("pkScore", 0)
                .sample();

        team2 = entityBuilder(GameTeam.class)
                .set("id", 2L)
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
            game.score(scorer, 1);

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
            game.score(scorer, 1);

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
                    .set("id", 999L)
                    .set("game", game2)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.score(scorer, 1))
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
    class OwnGoalScoreTest {

        @Test
        void team1_선수의_자책골이면_team2가_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .sample();

            // when
            game.scoreOwnGoal(scorer, 1);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(1)
            );
        }

        @Test
        void team2_선수의_자책골이면_team1이_득점한다() {
            // given
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team2)
                    .sample();

            // when
            game.scoreOwnGoal(scorer, 1);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(1),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void 참여하지_않는_선수는_자책골을_기록할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .set("id", 999L)
                    .set("game", game2)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.scoreOwnGoal(scorer, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 축구가_아닌_경기에서는_자책골을_기록할_수_없다() {
            // given
            League basketballLeague = entityBuilder(League.class)
                    .set("sportType", SportType.BASKETBALL)
                    .sample();
            Game basketballGame = entityBuilder(Game.class)
                    .set("id", 3L)
                    .set("league", basketballLeague)
                    .set("gameTeams", new ArrayList<>())
                    .sample();
            GameTeam basketballTeam1 = entityBuilder(GameTeam.class)
                    .set("id", 3L)
                    .set("game", basketballGame)
                    .sample();
            GameTeam basketballTeam2 = entityBuilder(GameTeam.class)
                    .set("id", 4L)
                    .set("game", basketballGame)
                    .sample();
            basketballGame.addGameTeam(basketballTeam1);
            basketballGame.addGameTeam(basketballTeam2);

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", basketballTeam1)
                    .sample();

            // when then
            assertThatThrownBy(() -> basketballGame.scoreOwnGoal(scorer, 1))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void 상대_팀이_없으면_자책골을_기록할_수_없다() {
            // given
            League soloTeamLeague = entityBuilder(League.class)
                    .set("sportType", SportType.SOCCER)
                    .sample();
            Game soloTeamGame = entityBuilder(Game.class)
                    .set("id", 4L)
                    .set("league", soloTeamLeague)
                    .set("gameTeams", new ArrayList<>())
                    .sample();
            GameTeam onlyTeam = entityBuilder(GameTeam.class)
                    .set("id", 5L)
                    .set("game", soloTeamGame)
                    .sample();
            soloTeamGame.addGameTeam(onlyTeam);

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", onlyTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> soloTeamGame.scoreOwnGoal(scorer, 1))
                    .isInstanceOf(BadRequestException.class);
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
            game.score(team1Player, 1);

            // when
            game.cancelScore(team1Player, 1);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void team1_선수의_자책골로_인한_team2의_득점을_취소한다() {
            // given
            game.scoreOwnGoal(team1Player, 1);

            // when
            game.cancelOwnGoalScore(team1Player, 1);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void 참여하지_않는_선수는_자책골_득점을_취소할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .set("id", 999L)
                    .set("game", game2)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.cancelOwnGoalScore(scorer, 1))
                    .isInstanceOf(IllegalArgumentException.class);
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
            game.score(team2Player, 1);

            // when
            game.cancelScore(team2Player, 1);

            // then
            assertAll(
                    () -> assertThat(team1.getScore()).isEqualTo(0),
                    () -> assertThat(team2.getScore()).isEqualTo(0)
            );
        }

        @Test
        void 동점_상황에서_한_팀만_점수를_취소한다() {
            // given
            game.score(team1Player, 1);
            game.score(team2Player, 1);

            // when
            game.cancelScore(team1Player, 1);

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
                    .set("id", 999L)
                    .set("game", game2)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.cancelScore(scorer, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 참여하지_않는_선수는_승부차기_득점을_취소할_수_없다() {
            // given
            GameTeam otherTeam = entityBuilder(GameTeam.class)
                    .set("id", 999L)
                    .set("game", game2)
                    .sample();

            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", otherTeam)
                    .sample();

            // when then
            assertThatThrownBy(() -> game.cancelPkScore(scorer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결과를 갱신할 때")
    class UpdateResultTest {

        @Test
        void 종료된_경기에서만_결과를_계산한다() {
            // given
            team1.score(1);
            game.updateState(GameState.PLAYING);
            GameResult initialTeam1Result = team1.getResult();
            GameResult initialTeam2Result = team2.getResult();

            // when
            game.updateResult();

            // then
            assertAll(
                    () -> assertThat(team1.getResult()).isEqualTo(initialTeam1Result),
                    () -> assertThat(team2.getResult()).isEqualTo(initialTeam2Result)
            );

            // when
            game.updateState(GameState.FINISHED);
            game.updateResult();

            // then
            assertAll(
                    () -> assertThat(team1.getResult()).isEqualTo(GameResult.WIN),
                    () -> assertThat(team2.getResult()).isEqualTo(GameResult.LOSE)
            );
        }

        @Test
        void 동점이면_무승부로_기록된다() {
            // given
            team1.score(1);
            team2.score(1);
            game.updateState(GameState.FINISHED);

            // when
            game.updateResult();

            // then
            assertAll(
                    () -> assertThat(team1.getResult()).isEqualTo(GameResult.DRAW),
                    () -> assertThat(team2.getResult()).isEqualTo(GameResult.DRAW)
            );
        }

        @Test
        void 동점이고_승부차기를_진행했다면_승부차기_점수로_승자를_결정한다() {
            // given
            Game pkGame = entityBuilder(Game.class)
                    .set("id", 3L)
                    .set("gameTeams", new ArrayList<>())
                    .set("isPkTaken", true)
                    .sample();
            GameTeam pkTeam1 = entityBuilder(GameTeam.class)
                    .set("id", 1L).set("game", pkGame).set("score", 1).set("pkScore", 4).sample();
            GameTeam pkTeam2 = entityBuilder(GameTeam.class)
                    .set("id", 2L).set("game", pkGame).set("score", 1).set("pkScore", 3).sample();
            pkGame.addGameTeam(pkTeam1);
            pkGame.addGameTeam(pkTeam2);
            pkGame.updateState(GameState.FINISHED);

            // when
            pkGame.updateResult();

            // then
            assertAll(
                    () -> assertThat(pkTeam1.getResult()).isEqualTo(GameResult.WIN),
                    () -> assertThat(pkTeam2.getResult()).isEqualTo(GameResult.LOSE)
            );
        }

        @Test
        void 승부차기_점수까지_동점이면_무승부로_기록된다() {
            // given
            Game pkGame = entityBuilder(Game.class)
                    .set("id", 3L)
                    .set("gameTeams", new ArrayList<>())
                    .set("isPkTaken", true)
                    .sample();
            GameTeam pkTeam1 = entityBuilder(GameTeam.class)
                    .set("id", 1L).set("game", pkGame).set("score", 1).set("pkScore", 3).sample();
            GameTeam pkTeam2 = entityBuilder(GameTeam.class)
                    .set("id", 2L).set("game", pkGame).set("score", 1).set("pkScore", 3).sample();
            pkGame.addGameTeam(pkTeam1);
            pkGame.addGameTeam(pkTeam2);
            pkGame.updateState(GameState.FINISHED);

            // when
            pkGame.updateResult();

            // then
            assertAll(
                    () -> assertThat(pkTeam1.getResult()).isEqualTo(GameResult.DRAW),
                    () -> assertThat(pkTeam2.getResult()).isEqualTo(GameResult.DRAW)
            );
        }

        @Test
        void 참가팀이_2팀이_아니면_결과를_계산하지_않는다() {
            // given
            GameTeam singleTeam = entityBuilder(GameTeam.class)
                    .set("id", 999L)
                    .set("game", game2)
                    .set("score", 1)
                    .set("pkScore", 0)
                    .sample();
            game2.addGameTeam(singleTeam);
            game2.updateState(GameState.FINISHED);
            GameResult initialResult = singleTeam.getResult();

            // when
            game2.updateResult();

            // then
            assertThat(singleTeam.getResult()).isEqualTo(initialResult);
        }
    }

    @Test
    void 주장_상태를_변경할_때_게임에_속하지_않는_게임팀에_대한_요청인_경우_예외를_던진다() {
        // given
        GameTeam team3 = entityBuilder(GameTeam.class)
                .set("id", 999L)
                .set("game", null)
                .sample();

        LineupPlayer lineupPlayer = entityBuilder(LineupPlayer.class)
                .set("gameTeam", team3)
                .sample();

        // when & then
        assertThatThrownBy(() -> game.changePlayerToCaptain(lineupPlayer))
                .hasMessage("해당 게임팀은 이 게임에 포함되지 않습니다.")
                .isInstanceOf(CustomException.class);
    }
}
