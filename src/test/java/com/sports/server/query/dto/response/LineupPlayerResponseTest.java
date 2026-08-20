package com.sports.server.query.dto.response;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Position;
import com.sports.server.command.team.domain.Team;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("라인업 응답의 포지션 노출은")
class LineupPlayerResponseTest {

    private GameTeam gameTeam;

    @BeforeEach
    void setUp() {
        Team team = entityBuilder(Team.class).set("name", "팀A").sample();
        gameTeam = entityBuilder(GameTeam.class).set("team", team).sample();
    }

    @Nested
    @DisplayName("선발 전원의 포지션이 등록됐으면")
    class AllStartersHavePosition {

        @Test
        void 포지션을_내려주고_FW_MF_DF_GK_순으로_정렬한다() {
            // given: 등록 순서는 GK → CB → CM → ST
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("골키퍼", Position.GK),
                    starter("수비수", Position.CB),
                    starter("미드필더", Position.CM),
                    starter("공격수", Position.ST)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then
            assertAll(
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::playerName)
                            .containsExactly("공격수", "미드필더", "수비수", "골키퍼"),
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsExactly(Position.ST, Position.CM, Position.CB, Position.GK)
            );
        }

        @Test
        void 포지션이_없는_후보가_교체_투입돼도_노출을_유지한다() {
            // given: 후보는 포지션 미등록이지만 교체로 출전 중(isPlaying=true)
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("공격수", Position.ST),
                    starter("수비수", Position.CB),
                    substituteInWithoutPosition("교체투입")
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: 게이트는 state 기준이라 선발 전원이 등록돼 있으면 그대로 노출된다
            assertThat(response.starterPlayers())
                    .extracting(LineupPlayerResponse.PlayerResponse::position)
                    .contains(Position.ST, Position.CB);
        }
    }

    @Nested
    @DisplayName("선발 중 한 명이라도 포지션이 없으면")
    class SomeStarterHasNoPosition {

        @Test
        void 라인업_전체의_포지션을_null_로_내린다() {
            // given
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("공격수", Position.ST),
                    starter("포지션없음", null),
                    candidate("후보", Position.GK)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then
            assertAll(
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsOnlyNulls(),
                    () -> assertThat(response.candidatePlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsOnlyNulls()
            );
        }
    }

    private LineupPlayer starter(String playerName, Position position) {
        return lineupPlayer(playerName, position, LineupPlayerState.STARTER, true);
    }

    private LineupPlayer candidate(String playerName, Position position) {
        return lineupPlayer(playerName, position, LineupPlayerState.CANDIDATE, false);
    }

    private LineupPlayer substituteInWithoutPosition(String playerName) {
        return lineupPlayer(playerName, null, LineupPlayerState.CANDIDATE, true);
    }

    private LineupPlayer lineupPlayer(String playerName, Position position, LineupPlayerState state,
                                      boolean isPlaying) {
        Player player = entityBuilder(Player.class).set("name", playerName).sample();
        return entityBuilder(LineupPlayer.class)
                .set("gameTeam", gameTeam)
                .set("player", player)
                .set("position", position)
                .set("state", state)
                .set("isPlaying", isPlaying)
                .set("isCaptain", false)
                .set("replacedPlayer", null)
                .sample();
    }
}
