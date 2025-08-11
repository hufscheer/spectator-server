package com.sports.server.command.league.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.exception.TeamErrorMessages;
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
                    .hasMessage(TeamErrorMessages.INVALID_LEAGUE_TEAMS_REQUEST_EXCEPTION);
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
                    .hasMessage(TeamErrorMessages.TEAMS_NOT_EXIST_INCLUDED_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("리그에서 참가 팀을 삭제할 때")
    class RemoveLeagueTeamTest {
        @Test
        void 리그팀에_포함되지_않는_팀은_삭제할_수_없다(){
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(100L, 101L));

            // when & then
            assertThatThrownBy(
                    () -> leagueService.removeTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_EXIST_IN_LEAGUE_TEAM_EXCEPTION);
        }

        @Test
        void 빈_팀_리스트이면_예외가_발생한다(){
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);
            LeagueRequest.Teams teamsRequest = new LeagueRequest.Teams(List.of(100L, 101L));

            // when & then
            assertThatThrownBy(
                    () -> leagueService.removeTeams(manager, leagueId, teamsRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(LeagueErrorMessages.TEAMS_NOT_EXIST_IN_LEAGUE_TEAM_EXCEPTION);
        }
    }
}
