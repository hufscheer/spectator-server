package com.sports.server.query.application;

import com.sports.server.command.league.application.LeagueStatisticsService;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.query.dto.response.*;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

@Sql(scripts = "/team-query-fixture.sql")
public class TeamQueryServiceTest extends ServiceTest {

    @Autowired
    private TeamQueryService teamQueryService;

    @Autowired
    private LeagueStatisticsService leagueStatisticsService;

    @Nested
    @DisplayName("단위별 팀 목록 조회 시")
    class GetAllTeamsByUnitsTest {

        @Test
        void 필터링할_단위가_없으면_모든_팀을_조회한다() {
            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(null);

            // then
            assertThat(responses).hasSize(7);
        }

        @Test
        void 하나의_단위로_필터링하여_조회한다() {
            // given
            List<String> units = List.of("사회과학대학");

            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(units);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(2),
                    () -> assertThat(responses.get(0).name()).isEqualTo("팀A"),
                    () -> assertThat(responses.get(1).name()).isEqualTo("팀D")
            );
        }

        @Test
        void 여러_단위로_필터링하여_조회한다() {
            // given
            List<String> units = List.of("사회과학대학", "기타");

            // when
            List<TeamResponse> responses = teamQueryService.getAllTeamsByUnits(units);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(6)
            );
        }

        @Test
        void 존재하지_않는_단위로_필터링하면_예외가_발생한다() {
            // given
            List<String> units = List.of("INVALID UNIT");

            // when & then
            assertThatThrownBy(() -> teamQueryService.getAllTeamsByUnits(units))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("팀 소속 선수 목록 조회 시")
    class GetAllTeamPlayersTest {

        @Test
        void 성공적으로_팀의_모든_선수를_조회한다() {
            // given
            Long teamId = 1L; // 팀A

            // when
            List<PlayerResponse> responses = teamQueryService.getAllTeamPlayers(teamId);

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(5),
                    () -> assertThat(responses).extracting(PlayerResponse::name)
                            .containsExactlyInAnyOrder("선수1", "선수2", "선수3", "선수4", "선수5"),
                    () -> assertThat(responses).extracting(PlayerResponse::totalGoalCount)
                            .containsExactlyInAnyOrder(0, 1, 3, 0, 0)
            );
        }
    }

    @Nested
    @DisplayName("팀 상세 정보 조회 시")
    class GetTeamDetailTest {

        @Test
        void 팀의_승무패_횟수를_성공적으로_조회한다() {
            // given
            Long teamAId = 1L;
            Long teamBId = 2L;
            Long teamCId = 3L;

            // when
            TeamDetailResponse responseOfTeamA = teamQueryService.getTeamDetail(teamAId);
            TeamDetailResponse responseOfTeamB = teamQueryService.getTeamDetail(teamBId);
            TeamDetailResponse responseOfTeamC = teamQueryService.getTeamDetail(teamCId);

            // then
            assertAll(
                    () -> assertThat(responseOfTeamA.name()).isEqualTo("팀A"),
                    () -> assertThat(responseOfTeamA.winCount()).isZero(),
                    () -> assertThat(responseOfTeamA.drawCount()).isZero(),
                    () -> assertThat(responseOfTeamA.loseCount()).isEqualTo(1),

                    () -> assertThat(responseOfTeamB.name()).isEqualTo("팀B"),
                    () -> assertThat(responseOfTeamB.winCount()).isEqualTo(1),
                    () -> assertThat(responseOfTeamB.drawCount()).isZero(),
                    () -> assertThat(responseOfTeamB.loseCount()).isEqualTo(1),

                    () -> assertThat(responseOfTeamC.name()).isEqualTo("팀C"),
                    () -> assertThat(responseOfTeamC.winCount()).isEqualTo(1),
                    () -> assertThat(responseOfTeamC.drawCount()).isZero(),
                    () -> assertThat(responseOfTeamC.loseCount()).isZero()
            );
        }

        @Test
        void 성공적으로_팀의_모든_선수를_조회한다() {
            // given
            Long teamId = 1L; // 팀A

            // when
            TeamDetailResponse response = teamQueryService.getTeamDetail(teamId);

            // then
            assertAll(
                    () -> assertThat(response.teamPlayers()).hasSize(5),
                    () -> assertThat(response.teamPlayers()).extracting(PlayerResponse::name)
                            .containsExactlyInAnyOrder("선수1", "선수2", "선수3", "선수4", "선수5"),
                    () -> assertThat(response.teamPlayers()).extracting(PlayerResponse::totalGoalCount)
                            .containsExactlyInAnyOrder(0, 1, 3, 0, 0)
            );
        }

        @Test
        void 팀의_득점왕을_성공적으로_조회한다() {
            // given
            Long teamId = 1L;

            // when
            TeamDetailResponse response = teamQueryService.getTeamDetail(teamId);

            // then
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("팀A"),
                    () -> assertThat(response.topScorers()).hasSize(2),
                    () -> assertThat(response.topScorers().get(0).playerName()).isEqualTo("선수3"),
                    () -> assertThat(response.topScorers().get(0).totalGoals()).isEqualTo(3),
                    () -> assertThat(response.topScorers().get(0).admissionYear()).isEqualTo("23"),

                    () -> assertThat(response.topScorers().get(1).playerName()).isEqualTo("선수2"),
                    () -> assertThat(response.topScorers().get(1).totalGoals()).isEqualTo(1),
                    () -> assertThat(response.topScorers().get(1).admissionYear()).isEqualTo("21")
            );
        }

        @Test
        void 승부차기_점수는_totalGoals에_포함되지_않는다() {
            // given
            Long teamId = 2L; // 팀B

            // when
            TeamDetailResponse response = teamQueryService.getTeamDetail(teamId);

            // then
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("팀B"),
                    () -> assertThat(response.topScorers().get(0).playerName()).isEqualTo("마선수10"), // 득점 3, 승부차기 1
                    () -> assertThat(response.topScorers().get(0).totalGoals()).isEqualTo(3) // 득점만 포함
            );
        }

        @Test
        void 선수_총득점이_동일한_경우_이름_순서대로_정렬된다() {
            // given
            Long teamId = 2L; // 팀B

            // when
            TeamDetailResponse response = teamQueryService.getTeamDetail(teamId);

            // then
            assertAll(
                    () -> assertThat(response.name()).isEqualTo("팀B"),
                    () -> assertThat(response.topScorers()).hasSize(3),
                    () -> assertThat(response.topScorers().get(0).playerName()).isEqualTo("마선수10"),
                    () -> assertThat(response.topScorers().get(0).totalGoals()).isEqualTo(3),

                    () -> assertThat(response.topScorers().get(1).playerName()).isEqualTo("나선수7"),
                    () -> assertThat(response.topScorers().get(1).totalGoals()).isEqualTo(2),

                    () -> assertThat(response.topScorers().get(2).playerName()).isEqualTo("라선수9"),
                    () -> assertThat(response.topScorers().get(2).totalGoals()).isEqualTo(2)
            );
        }

        @Test
        void 리그의_우승_준우승_팀이_정상적으로_리그통계에_업데이트된다() {
            // given
            Long teamBId = 2L;
            Long teamCId = 3L;
            Long finalGameId = 2L;

            // when
            leagueStatisticsService.updateLeagueStatisticFromFinalGame(finalGameId);
            TeamDetailResponse responseOfTeamB = teamQueryService.getTeamDetail(teamBId);
            TeamDetailResponse responseOfTeamC = teamQueryService.getTeamDetail(teamCId);

            // then
            assertAll(
                    () -> assertThat(responseOfTeamB.name()).isEqualTo("팀B"),
                    () -> assertThat(responseOfTeamB.trophies().get(0).trophyType()).isEqualTo("준우승"),
                    () -> assertThat(responseOfTeamC.name()).isEqualTo("팀C"),
                    () -> assertThat(responseOfTeamC.trophies().get(0).trophyType()).isEqualTo("우승")
            );
        }

        @Nested
        @DisplayName("팀별보기 상세페이지 조회 시")
        class GetAllTeamsSummaryTest {

            private List<TeamSummaryResponse> responses;
            private TeamSummaryResponse teamAResponse;
            private TeamSummaryResponse teamBResponse;
            private TeamSummaryResponse teamDResponse;

            @BeforeEach
            void setUp() {
                List<String> units = List.of("사회과학대학", "기타");
                Long finalGameId = 2L;

                leagueStatisticsService.updateLeagueStatisticFromFinalGame(finalGameId);

                this.responses = teamQueryService.getAllTeamsSummary(units);
                this.teamAResponse = responses.get(3);
                this.teamBResponse = responses.get(4);
                this.teamDResponse = responses.get(5);
            }

            @Test
            void 전체_팀이_정상적으로_반환된다() {
                assertAll(
                        () -> assertThat(responses).hasSize(6),
                        () -> assertThat(teamAResponse.teamDetail().name()).isEqualTo("팀A"),
                        () -> assertThat(teamBResponse.teamDetail().name()).isEqualTo("팀B"),
                        () -> assertThat(teamDResponse.teamDetail().name()).isEqualTo("팀D")
                );
            }

            @Test
            void 팀A의_상세_정보가_정상적으로_반환된다() {
                TeamDetailResponse teamADetail = teamAResponse.teamDetail();
                TeamDetailResponse.TeamTopScorer teamATopScorer = teamADetail.topScorers().get(0);
                
                assertAll(
                        () -> assertThat(teamADetail.name()).isEqualTo("팀A"),
                        () -> assertThat(teamADetail.drawCount()).isZero(),
                        () -> assertThat(teamADetail.winCount()).isZero(),
                        () -> assertThat(teamADetail.loseCount()).isEqualTo(1),
                        () -> assertThat(teamADetail.topScorers()).hasSize(2),
                        
                        () -> assertThat(teamATopScorer.playerName()).isEqualTo("선수3"),
                        () -> assertThat(teamATopScorer.totalGoals()).isEqualTo(3),
                        () -> assertThat(teamATopScorer.admissionYear()).isEqualTo("23"),
                        
                        () -> assertThat(teamAResponse.recentGames()).hasSize(1)
                );
            }

            @Test
            void 팀B의_상세_정보가_정상적으로_반환된다() {
                TeamDetailResponse teamBDetail = teamBResponse.teamDetail();
                TeamDetailResponse.TeamTopScorer teamBTopScorer1 = teamBDetail.topScorers().get(0);
                TeamDetailResponse.TeamTopScorer teamBTopScorer2 = teamBDetail.topScorers().get(1);
                TeamDetailResponse.TeamTopScorer teamBTopScorer3 = teamBDetail.topScorers().get(2);
                
                assertAll(
                        () -> assertThat(teamBDetail.name()).isEqualTo("팀B"),
                        () -> assertThat(teamBDetail.winCount()).isEqualTo(1),
                        () -> assertThat(teamBDetail.drawCount()).isZero(),
                        () -> assertThat(teamBDetail.loseCount()).isEqualTo(1),
                        () -> assertThat(teamBDetail.topScorers()).hasSize(3),

                        () -> assertThat(teamBTopScorer1.playerName()).isEqualTo("마선수10"),
                        () -> assertThat(teamBTopScorer1.totalGoals()).isEqualTo(3),
                        () -> assertThat(teamBTopScorer1.rank()).isEqualTo(1),
                        () -> assertThat(teamBTopScorer2.playerName()).isEqualTo("나선수7"),
                        () -> assertThat(teamBTopScorer2.totalGoals()).isEqualTo(2),
                        () -> assertThat(teamBTopScorer2.rank()).isEqualTo(2),
                        () -> assertThat(teamBTopScorer3.playerName()).isEqualTo("라선수9"),
                        () -> assertThat(teamBTopScorer3.totalGoals()).isEqualTo(2),
                        () -> assertThat(teamBTopScorer3.rank()).isEqualTo(2),

                        () -> assertThat(teamBDetail.trophies().get(0).trophyType()).isEqualTo("준우승"),
                        () -> assertThat(teamBResponse.recentGames()).hasSize(2)
                );
            }
        }
    }

    @Nested
    @DisplayName("팀별 최근 경기 조회 시")
    class GetRecentGamesTest {

        @Test
        void 게임이_없는_팀은_빈_리스트를_반환한다() {
            // given
            List<String> units = List.of("기타");

            // when
            List<TeamSummaryResponse> responses = teamQueryService.getAllTeamsSummary(units);

            TeamSummaryResponse noGameTeamResponse = responses.stream()
                    .filter(response -> response.teamDetail().name().equals("게임없는팀"))
                    .findFirst()
                    .orElseThrow();

            // then
            assertAll(
                    () -> assertThat(noGameTeamResponse.teamDetail().name()).isEqualTo("게임없는팀"),
                    () -> assertThat(noGameTeamResponse.recentGames()).isEmpty()
            );
        }

        @Test
        void 게임이_2개_이하인_팀은_해당_게임만_반환한다() {
            // given
            List<String> units = List.of("기타");

            // when
            List<TeamSummaryResponse> responses = teamQueryService.getAllTeamsSummary(units);

            TeamSummaryResponse fewGameTeamResponse = responses.stream()
                    .filter(response -> response.teamDetail().name().equals("게임2개팀"))
                    .findFirst()
                    .orElseThrow();

            // then
            assertAll(
                    () -> assertThat(fewGameTeamResponse.teamDetail().name()).isEqualTo("게임2개팀"),
                    () -> assertThat(fewGameTeamResponse.recentGames()).hasSize(2),
                    () -> assertThat(fewGameTeamResponse.recentGames().get(0).gameName()).isEqualTo("게임2개팀 게임1"),
                    () -> assertThat(fewGameTeamResponse.recentGames().get(1).gameName()).isEqualTo("게임2개팀 게임2")
            );
        }

        @Test
        void 게임이_4개_이상인_팀은_최근_3개만_반환한다() {
            // given
            List<String> units = List.of("기타");

            // when
            List<TeamSummaryResponse> responses = teamQueryService.getAllTeamsSummary(units);

            TeamSummaryResponse manyGameTeamResponse = responses.stream()
                    .filter(response -> response.teamDetail().name().equals("게임많은팀"))
                    .findFirst()
                    .orElseThrow();

            // then
            assertAll(
                    () -> assertThat(manyGameTeamResponse.teamDetail().name()).isEqualTo("게임많은팀"),
                    () -> assertThat(manyGameTeamResponse.recentGames()).hasSize(3),
                    () -> assertThat(manyGameTeamResponse.recentGames().get(0).gameName()).isEqualTo("최근게임1"),
                    () -> assertThat(manyGameTeamResponse.recentGames().get(1).gameName()).isEqualTo("최근게임2"),
                    () -> assertThat(manyGameTeamResponse.recentGames().get(2).gameName()).isEqualTo("최근게임3")
            );
        }

        @Test
        void 최근_게임들은_시작_날짜_최신_순으로_정렬된다() {
            // given
            List<String> units = List.of("기타");

            // when
            List<TeamSummaryResponse> responses = teamQueryService.getAllTeamsSummary(units);

            TeamSummaryResponse manyGameTeamResponse = responses.stream()
                    .filter(response -> response.teamDetail().name().equals("게임많은팀"))
                    .findFirst()
                    .orElseThrow();

            List<LocalDateTime> startTimes = manyGameTeamResponse.recentGames().stream()
                    .map(GameDetailResponse::startTime)
                    .toList();

            // then
            assertThat(startTimes).isSortedAccordingTo(Comparator.reverseOrder());
        }

        @Test
        void 여러_팀이_조회될_경우에도_각각_올바르게_처리된다() {
            // given
            List<String> units = List.of("기타");

            // when
            List<TeamSummaryResponse> responses = teamQueryService.getAllTeamsSummary(units);

            TeamSummaryResponse noGameTeam = responses.stream()
                    .filter(r -> r.teamDetail().name().equals("게임없는팀"))
                    .findFirst().orElseThrow();
            
            TeamSummaryResponse twoGameTeam = responses.stream()
                    .filter(r -> r.teamDetail().name().equals("게임2개팀"))
                    .findFirst().orElseThrow();
            
            TeamSummaryResponse manyGameTeam = responses.stream()
                    .filter(r -> r.teamDetail().name().equals("게임많은팀"))
                    .findFirst().orElseThrow();

            // then
            assertAll(
                    () -> assertThat(noGameTeam.recentGames()).isEmpty(),
                    () -> assertThat(twoGameTeam.recentGames()).hasSize(2),
                    () -> assertThat(manyGameTeam.recentGames()).hasSize(3)
            );
        }
    }
}
