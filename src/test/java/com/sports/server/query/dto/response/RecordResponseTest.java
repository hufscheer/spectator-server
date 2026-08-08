package com.sports.server.query.dto.response;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecordResponseTest {

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

        Team teamA = entityBuilder(Team.class)
                .set("name", "팀A")
                .set("logoImageUrl", "http://example.com/logo_a.png")
                .sample();
        Team teamB = entityBuilder(Team.class)
                .set("name", "팀B")
                .set("logoImageUrl", "http://example.com/logo_b.png")
                .sample();

        team1 = entityBuilder(GameTeam.class)
                .set("id", 1L)
                .set("game", game)
                .set("team", teamA)
                .set("score", 1)
                .sample();

        team2 = entityBuilder(GameTeam.class)
                .set("id", 2L)
                .set("game", game)
                .set("team", teamB)
                .set("score", 2)
                .sample();

        game.addGameTeam(team1);
        game.addGameTeam(team2);
    }

    @Nested
    @DisplayName("자책골 타임라인을 변환하면")
    class OwnGoalTest {

        @Test
        void 득점자_이름과_상대팀_정보를_담아_변환한다() {
            // given
            Player player = entityBuilder(Player.class)
                    .set("name", "선수1")
                    .sample();
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team1)
                    .set("player", player)
                    .sample();
            OwnGoalTimeline timeline = OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team1, 1
            );
            timeline.apply();

            // when
            RecordResponse actual = RecordResponse.from(timeline);

            // then
            assertAll(
                    () -> assertThat(actual.type()).isEqualTo("OWN_GOAL"),
                    () -> assertThat(actual.playerName()).isEqualTo(scorer.getPlayer().getName()),
                    () -> assertThat(actual.gameTeamId()).isEqualTo(team2.getId()),
                    () -> assertThat(actual.teamName()).isEqualTo("팀B"),
                    () -> assertThat(actual.teamImageUrl()).isEqualTo("http://example.com/logo_b.png"),
                    () -> assertThat(actual.scoreRecord()).isNull(),
                    () -> assertThat(actual.ownGoalRecord()).isNotNull(),
                    () -> assertThat(actual.ownGoalRecord().score()).isEqualTo(1)
            );
        }

        @Test
        void team2_선수의_자책골이면_team1_정보를_담아_변환한다() {
            // given
            Player player = entityBuilder(Player.class)
                    .set("name", "선수6")
                    .sample();
            LineupPlayer scorer = entityBuilder(LineupPlayer.class)
                    .set("gameTeam", team2)
                    .set("player", player)
                    .sample();
            OwnGoalTimeline timeline = OwnGoalTimeline.of(
                    game, SoccerQuarter.FIRST_HALF, 10, scorer, team2, 1
            );
            timeline.apply();

            // when
            RecordResponse actual = RecordResponse.from(timeline);

            // then
            assertAll(
                    () -> assertThat(actual.gameTeamId()).isEqualTo(team1.getId()),
                    () -> assertThat(actual.teamName()).isEqualTo("팀A")
            );
        }
    }
}
