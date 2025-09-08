package com.sports.server.command.league.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@Sql("/league-fixture.sql")
public class LeagueServiceTest extends ServiceTest {
    @Autowired
    private LeagueService leagueService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Nested
    @DisplayName("리그를 삭제할 때")
    class LeagueDeleteTest {
        @Test
        void 삭제한_이후에는_해당_객체를_찾을_수_없다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            leagueService.delete(manager, leagueId);

            // then
            assertThatThrownBy(
                    () -> entityUtils.getEntity(leagueId, League.class))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 권한이_없는_멤버는_리그를_삭제할_수_없다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(
                    () -> leagueService.delete(manager, leagueId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("리그에 참가 팀을 추가할 때")
    class AddLeagueTeamTest {
        @Test
        void 이미_대회에_참가중인_팀을_제외하고_추가된다(){
            // given
            Long leagueId = 1L; // 기존 참가팀 개수 3개
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(1L, 4L, 5L)); // 1L은 이미 참가, 2개 팀 추가

            // when
            League league = leagueService.addTeams(manager, leagueId, teamsRequest);

            // then
            assertThat(league.getLeagueTeams().size()).isEqualTo(5);
        }

        @Test
        void 팀_리스트가_비어있을_경우_예외가_발생한다(){
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of());

            // when & then
            assertThatThrownBy(
                    () -> leagueService.addTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("추가할 수 있는 팀이 존재하지 않습니다.");
        }

        @Test
        void 존재하지_않는_팀은_추가할_수_없다(){
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(100L, 101L));

            // when & then
            assertThatThrownBy(
                    () -> leagueService.addTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("리그에서 참가 팀을 삭제할 때")
    class RemoveLeagueTeamTest {
        @Test
        void 리그팀에_포함되지_않는_팀은_삭제할_수_없다(){
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(100L, 101L));

            // when & then
            assertThatThrownBy(
                    () -> leagueService.removeTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
        }

        @Test
        void 빈_팀_리스트이면_예외가_발생한다(){
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(100L, 101L));

            // when & then
            assertThatThrownBy(
                    () -> leagueService.removeTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
        }

        @Test
        void 삭제한_이후에는_해당_팀_객체를_찾을_수_없다(){
            // given
            Long leagueId = 1L;
            Long teamIdToRemove = 1L;
            Long leagueTeamIdToRemove = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(teamIdToRemove));
            leagueService.removeTeams(manager, leagueId, teamsRequest);

            // then
            assertThatThrownBy(
                    () -> entityUtils.getEntity(leagueTeamIdToRemove, LeagueTeam.class))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("리그의 총 응원수와 응원톡 수를 업데이트할 때")
    class UpdateTotalCheerCountsAndTotalTalkCountTest {
        @Test
        void 리그의_모든_팀의_총_응원수와_응원톡_수가_정확히_집계된다() {
            // given
            Long leagueId = 1L; // 테스트 데이터: 팀1(150 응원, 2 응원톡), 팀2(100 응원, 2 응원톡), 팀3(75 응원, 1 응원톡)

            // when
            leagueService.updateTotalCheerCountsAndTotalTalkCount(leagueId);

            // then
            List<LeagueTeam> leagueTeams = leagueTeamRepository.findByLeagueId(leagueId);

            LeagueTeam team1 = leagueTeams.stream()
                    .filter(lt -> lt.getTeam().getId().equals(1L))
                    .findFirst().orElseThrow();
            LeagueTeam team2 = leagueTeams.stream()
                    .filter(lt -> lt.getTeam().getId().equals(2L))
                    .findFirst().orElseThrow();
            LeagueTeam team3 = leagueTeams.stream()
                    .filter(lt -> lt.getTeam().getId().equals(3L))
                    .findFirst().orElseThrow();

            assertThat(team1.getTotalCheerCount()).isEqualTo(150);
            assertThat(team1.getTotalTalkCount()).isEqualTo(2);
            
            assertThat(team2.getTotalCheerCount()).isEqualTo(100);
            assertThat(team2.getTotalTalkCount()).isEqualTo(2);
            
            assertThat(team3.getTotalCheerCount()).isEqualTo(75);
            assertThat(team3.getTotalTalkCount()).isEqualTo(1);
        }

        @Test
        void 응원수나_응원톡이_없는_팀은_0으로_설정된다() {
            // given
            Long leagueId = 2L; // 테스트 데이터: 팀4(30 응원, 1 응원톡), 팀5(0 응원, 0 응원톡)

            // when
            leagueService.updateTotalCheerCountsAndTotalTalkCount(leagueId);

            // then
            List<LeagueTeam> leagueTeams = leagueTeamRepository.findByLeagueId(leagueId);

            LeagueTeam team4 = leagueTeams.stream()
                    .filter(lt -> lt.getTeam().getId().equals(4L))
                    .findFirst().orElseThrow();
            LeagueTeam team5 = leagueTeams.stream()
                    .filter(lt -> lt.getTeam().getId().equals(5L))
                    .findFirst().orElseThrow();

            assertThat(team4.getTotalCheerCount()).isEqualTo(30);
            assertThat(team4.getTotalTalkCount()).isEqualTo(1);
            
            assertThat(team5.getTotalCheerCount()).isEqualTo(0);
            assertThat(team5.getTotalTalkCount()).isEqualTo(0);
        }
    }
}
