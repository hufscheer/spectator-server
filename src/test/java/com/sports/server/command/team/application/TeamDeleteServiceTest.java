package com.sports.server.command.team.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql("/team-delete-fixture.sql")
@DisplayName("팀을 삭제할 때")
public class TeamDeleteServiceTest extends ServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private MemberRepository memberRepository;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmailWithOrganization("john@example.com").orElseThrow();
    }

    @Test
    void 아무_데도_엮이지_않은_팀은_삭제된다() {
        // given
        Long teamId = 1L;

        // when
        teamService.delete(manager, teamId);

        // then
        assertThatThrownBy(() -> entityUtils.getEntity(teamId, Team.class))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * 팀은 소프트 삭제라 teams 행이 남지만 조회 시 @Where 에 걸려 보이지 않는다.
     * game_teams 가 계속 그 팀을 가리키면 경기 조회가 EntityNotFoundException 으로 500 이 된다.
     */
    @Test
    void 경기에_편성된_팀은_삭제할_수_없다() {
        // given
        Long teamInGame = 2L;

        // when & then
        assertThatThrownBy(() -> teamService.delete(manager, teamInGame))
                .isInstanceOf(CustomException.class)
                .hasMessage(TeamErrorMessages.TEAM_IN_GAME_DELETE_EXCEPTION);
    }

    @Test
    void 대회에_참가_중인_팀은_삭제할_수_없다() {
        // given
        Long teamInLeague = 3L;

        // when & then
        assertThatThrownBy(() -> teamService.delete(manager, teamInLeague))
                .isInstanceOf(CustomException.class)
                .hasMessage(TeamErrorMessages.TEAM_IN_LEAGUE_DELETE_EXCEPTION);
    }

    @Test
    void 삭제가_막힌_팀은_그대로_남아_있다() {
        // given
        Long teamInGame = 2L;

        // when
        assertThatThrownBy(() -> teamService.delete(manager, teamInGame))
                .isInstanceOf(CustomException.class);

        // then
        assertThatCode(() -> entityUtils.getEntity(teamInGame, Team.class))
                .doesNotThrowAnyException();
    }
}
