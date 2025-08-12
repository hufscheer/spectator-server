//package com.sports.server.command.league.application;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import com.sports.server.command.game.domain.Game;
//import com.sports.server.command.league.domain.League;
//import com.sports.server.command.league.domain.LeagueStatistics;
//import com.sports.server.command.league.domain.LeagueStatisticsRepository;
//import com.sports.server.command.league.domain.LeagueTeam;
//import com.sports.server.command.league.domain.LeagueTeamRepository;
//import com.sports.server.command.team.domain.Team;
//import com.sports.server.common.application.EntityUtils;
//import com.sports.server.support.ServiceTest;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.jdbc.Sql;
//
//@Sql("/league-fixture.sql")
//public class LeagueStatisticsServiceTest extends ServiceTest {
//
//    @Autowired
//    private LeagueStatisticsService leagueStatisticsService;
//
//    @Autowired
//    private LeagueStatisticsRepository leagueStatisticsRepository;
//
//    @Autowired
//    private LeagueTeamRepository leagueTeamRepository;
//
//    @Autowired
//    private EntityUtils entityUtils;
//
//    @Nested
//    @DisplayName("최종 게임으로부터 리그 통계를 업데이트할 때")
//    class UpdateLeagueStatisticFromFinalGameTest {
//
//        @Test
//        @DisplayName("유효한 게임이 주어지면 리그 통계가 업데이트된다")
//        void 유효한_게임이_주어지면_리그_통계가_업데이트된다() {
//            // given
//            Game finalGame = entityUtils.getEntity(1L, Game.class);
//            League league = finalGame.getLeague();
//
//            // when
//            leagueStatisticsService.updateLeagueStatisticFromFinalGame(finalGame);
//
//            // then
//            LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(league.getId());
//            assertThat(statistics).isNotNull();
//        }
//
//        @Test
//        @DisplayName("승리팀과 준우승팀이 올바르게 업데이트된다")
//        void 승리팀과_준우승팀이_올바르게_업데이트된다() {
//            // given
//            Game finalGame = entityUtils.getEntity(1L, Game.class);
//            League league = finalGame.getLeague();
//
//            // when
//            leagueStatisticsService.updateLeagueStatisticFromFinalGame(finalGame);
//
//            // then
//            LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(league.getId());
//            assertThat(statistics.getFirstWinnerTeam()).isNotNull();
//            assertThat(statistics.getSecondWinnerTeam()).isNotNull();
//
//            // 승리팀의 랭킹이 1위로 업데이트되었는지 확인
//            Team winnerTeam = statistics.getFirstWinnerTeam();
//            LeagueTeam winnerLeagueTeam = leagueTeamRepository.findByLeagueAndTeam(league, winnerTeam).orElse(null);
//            assertThat(winnerLeagueTeam).isNotNull();
//            assertThat(winnerLeagueTeam.getRanking()).isEqualTo(1);
//
//            // 준우승팀의 랭킹이 2위로 업데이트되었는지 확인
//            Team secondTeam = statistics.getSecondWinnerTeam();
//            LeagueTeam secondLeagueTeam = leagueTeamRepository.findByLeagueAndTeam(league, secondTeam).orElse(null);
//            assertThat(secondLeagueTeam).isNotNull();
//            assertThat(secondLeagueTeam.getRanking()).isEqualTo(2);
//        }
//
//        @Test
//        @DisplayName("최다 응원팀과 최다 대화팀이 올바르게 업데이트된다")
//        void 최다_응원팀과_최다_대화팀이_올바르게_업데이트된다() {
//            // given
//            Game finalGame = entityUtils.getEntity(1L, Game.class);
//            League league = finalGame.getLeague();
//
//            // when
//            leagueStatisticsService.updateLeagueStatisticFromFinalGame(finalGame);
//
//            // then
//            LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(league.getId());
//            assertThat(statistics.getMostCheeredTeam()).isNotNull();
//            assertThat(statistics.getMostCheerTalksTeam()).isNotNull();
//        }
//
//        @Test
//        @DisplayName("게임이 null이면 예외가 발생한다")
//        void 게임이_null이면_예외가_발생한다() {
//            // when & then
//            assertThatThrownBy(() -> leagueStatisticsService.updateLeagueStatisticFromFinalGame(null))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessage("유효한 게임 또는 리그 정보가 없습니다.");
//        }
//
//        @Test
//        @DisplayName("게임에 리그 정보가 없으면 예외가 발생한다")
//        void 게임에_리그_정보가_없으면_예외가_발생한다() {
//            // given
//            Game gameWithoutLeague = Game.builder()
//                    .league(null)
//                    .build();
//
//            // when & then
//            assertThatThrownBy(() -> leagueStatisticsService.updateLeagueStatisticFromFinalGame(gameWithoutLeague))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessage("유효한 게임 또는 리그 정보가 없습니다.");
//        }
//
//        @Test
//        @DisplayName("게임팀이 최소 팀 수보다 적으면 업데이트하지 않는다")
//        void 게임팀이_최소_팀_수보다_적으면_업데이트하지_않는다() {
//            // given
//            Game gameWithInsufficientTeams = entityUtils.getEntity(2L, Game.class); // 팀 수가 부족한 게임
//            League league = gameWithInsufficientTeams.getLeague();
//
//            // when
//            leagueStatisticsService.updateLeagueStatisticFromFinalGame(gameWithInsufficientTeams);
//
//            // then
//            LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(league.getId());
//            // 통계는 생성되지만 승리팀 정보는 업데이트되지 않음
//            assertThat(statistics).isNotNull();
//        }
//    }
//}