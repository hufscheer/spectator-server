package com.sports.server.query.dto.response;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.game.domain.Position;
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
    @DisplayName("선발 중 대분류까지만 입력된 선수가 있으면")
    class SomeStarterHasCategoryOnly {

        @Test
        void 전원을_대분류로_낮춰_내려준다() {
            // given: 수비수만 대분류(DF), 나머지는 세부까지 입력
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("골키퍼", Position.GK),
                    starter("수비수", Position.DF),
                    starter("미드필더", Position.CM),
                    starter("공격수", Position.ST)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: 상세도가 섞이지 않도록 세부 입력자도 대분류로 접힌다
            assertAll(
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::playerName)
                            .containsExactly("공격수", "미드필더", "수비수", "골키퍼"),
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsExactly(Position.FW, Position.MF, Position.DF, Position.GK)
            );
        }

        @Test
        void 후보는_포지션을_노출하지_않는다() {
            // given
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("공격수", Position.FW),
                    starter("골키퍼", Position.GK),
                    candidate("후보공격수", Position.ST)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: 후보는 등번호·이름만 보여주는 화면이라 포지션을 내리지 않는다
            assertAll(
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsExactly(Position.FW, Position.GK),
                    () -> assertThat(response.candidatePlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsOnlyNulls()
            );
        }
    }

    @Nested
    @DisplayName("선발 인원이 11명이 아니어도")
    class NonElevenLineup {

        @Test
        void 여자축구_8인제처럼_인원이_적어도_동일하게_동작한다() {
            // given: 선발 8명, 전원 세부 포지션 입력
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("골키퍼", Position.GK),
                    starter("왼쪽풀백", Position.LB),
                    starter("중앙수비", Position.CB),
                    starter("오른쪽풀백", Position.RB),
                    starter("중앙미드", Position.CM),
                    starter("오른쪽미드", Position.RM),
                    starter("스트라이커", Position.ST),
                    starter("왼쪽윙", Position.LW)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: 게이트는 인원 수가 아니라 선발 전원의 입력 여부만 본다
            assertAll(
                    () -> assertThat(response.starterPlayers()).hasSize(8),
                    () -> assertThat(response.starterPlayers())
                            .extracting(LineupPlayerResponse.PlayerResponse::position)
                            .containsExactly(Position.LW, Position.ST, Position.CM, Position.RM,
                                    Position.LB, Position.CB, Position.RB, Position.GK)
            );
        }

        @Test
        void 인원이_적어도_한_명이_비면_전체가_미표시된다() {
            // given: 선발 8명 중 한 명 미입력
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("골키퍼", Position.GK),
                    starter("수비수", Position.CB),
                    starter("미드필더", Position.CM),
                    starter("공격수", Position.ST),
                    starter("공격수2", Position.LW),
                    starter("수비수2", Position.LB),
                    starter("미드필더2", Position.RM),
                    starter("포지션없음", null)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then
            assertThat(response.starterPlayers())
                    .extracting(LineupPlayerResponse.PlayerResponse::position)
                    .containsOnlyNulls();
        }
    }

    @Nested
    @DisplayName("농구도 축구와 같은 대분류 규칙을 따라")
    class Basketball {

        @Test
        void 선발_전원이_세부까지_입력하면_세부로_내려준다() {
            // given
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("센터", Position.C),
                    starter("파워포워드", Position.PF),
                    starter("포인트가드", Position.PG)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: G → F → C 순, 값은 접히지 않는다
            assertThat(response.starterPlayers())
                    .extracting(LineupPlayerResponse.PlayerResponse::position)
                    .containsExactly(Position.PG, Position.PF, Position.C);
        }

        @Test
        void 대분류만_입력한_선발이_있으면_전원을_대분류로_낮춘다() {
            // given: 한 명만 "선택 안 함"으로 G 가 저장됐다
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("센터", Position.C),
                    starter("스몰포워드", Position.SF),
                    starter("가드", Position.G)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: SF 도 F 로 접힌다
            assertThat(response.starterPlayers())
                    .extracting(LineupPlayerResponse.PlayerResponse::position)
                    .containsExactly(Position.G, Position.F, Position.C);
        }

        @Test
        void 센터는_세부가_곧_대분류라_전체를_낮추지_않는다() {
            // given
            List<LineupPlayer> lineupPlayers = List.of(
                    starter("센터", Position.C),
                    starter("슈팅가드", Position.SG)
            );

            // when
            LineupPlayerResponse.All response = new LineupPlayerResponse.All(gameTeam, lineupPlayers);

            // then: SG 가 G 로 접히지 않는다
            assertThat(response.starterPlayers())
                    .extracting(LineupPlayerResponse.PlayerResponse::position)
                    .containsExactly(Position.SG, Position.C);
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
