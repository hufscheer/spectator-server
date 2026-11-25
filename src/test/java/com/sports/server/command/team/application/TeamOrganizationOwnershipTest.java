package com.sports.server.command.team.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.exception.ForbiddenException;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

// 팀 1, 선수 1, 선수 4는 학교 A, 팀 2, 선수 2, 선수 3은 학교 B. 슈퍼유저는 회원 1이다
@Sql("/organization-ownership-fixture.sql")
@DisplayName("팀 쓰기는 같은 학교 매니저, 그 학교 대회 관리자, 슈퍼유저만 할 수 있다")
class TeamOrganizationOwnershipTest extends ServiceTest {

    @Autowired
    private TeamService teamService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member(String email) {
        return memberRepository.findMemberByEmailWithOrganization(email).orElseThrow();
    }

    private Member managerA() {
        return member("a@example.com");
    }

    private Member managerB() {
        return member("b@example.com");
    }

    private String teamName(long id) {
        return jdbcTemplate.queryForObject("SELECT name FROM teams WHERE id = ?", String.class, id);
    }

    private long teamPlayerCount(long teamId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM team_players WHERE team_id = ?", Long.class, teamId);
    }

    private TeamRequest.Update rename(String name) {
        return new TeamRequest.Update(name, null, null, null, null);
    }

    @Test
    void 다른_학교_팀을_수정하면_403이고_팀은_그대로다() {
        assertThatThrownBy(() -> teamService.update(managerA(), rename("탈취"), 2L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamName(2L)).isEqualTo("학교 B 팀");
    }

    @Test
    void 다른_학교_팀을_삭제하면_403이고_팀은_남는다() {
        assertThatThrownBy(() -> teamService.delete(managerA(), 2L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamName(2L)).isEqualTo("학교 B 팀");
    }

    @Test
    void 다른_학교_팀의_로고를_지우면_403이고_로고는_그대로다() {
        assertThatThrownBy(() -> teamService.deleteLogoImage(managerA(), 2L))
                .isInstanceOf(ForbiddenException.class);

        String logo = jdbcTemplate.queryForObject("SELECT logo_image_url FROM teams WHERE id = 2", String.class);
        assertThat(logo).isEqualTo("https://example.com/logos/b.png");
    }

    @Test
    void 다른_학교_팀에_선수를_추가하면_403이고_팀_선수는_그대로다() {
        assertThatThrownBy(() -> teamService.addPlayersToTeam(managerA(), 2L,
                List.of(new TeamRequest.TeamPlayerRegister(3L, 10))))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamPlayerCount(2L)).isEqualTo(1);
    }

    @Test
    void 다른_학교_팀의_선수를_삭제하면_403이고_팀_선수는_그대로다() {
        assertThatThrownBy(() -> teamService.deleteTeamPlayer(managerA(), 2L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamPlayerCount(2L)).isEqualTo(1);
    }

    @Test
    void 다른_학교_선수를_내_팀에_추가하면_403이고_팀_선수는_그대로다() {
        assertThatThrownBy(() -> teamService.addPlayersToTeam(managerA(), 1L,
                List.of(new TeamRequest.TeamPlayerRegister(3L, 10))))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamPlayerCount(1L)).isEqualTo(1);
    }

    @Test
    void 같은_학교_매니저는_팀을_수정하고_선수를_추가하고_뺄_수_있다() {
        teamService.update(managerA(), rename("새 이름"), 1L);
        teamService.addPlayersToTeam(managerA(), 1L, List.of(new TeamRequest.TeamPlayerRegister(4L, 10)));
        assertThat(teamName(1L)).isEqualTo("새 이름");
        assertThat(teamPlayerCount(1L)).isEqualTo(2);

        teamService.deleteTeamPlayer(managerA(), 1L);
        assertThat(teamPlayerCount(1L)).isEqualTo(1);
    }

    @Test
    void 같은_학교_매니저는_팀을_삭제하고_로고를_지울_수_있다() {
        teamService.deleteLogoImage(managerA(), 1L);
        assertThat(jdbcTemplate.queryForObject("SELECT logo_image_url FROM teams WHERE id = 1", String.class))
                .isEmpty();

        teamService.delete(managerA(), 1L);
        assertThat(jdbcTemplate.queryForObject("SELECT is_deleted FROM teams WHERE id = 1", Boolean.class)).isTrue();
    }

    @Test
    void 슈퍼유저는_다른_학교_자원도_다룰_수_있다() {
        Member superUser = member("super@example.com");

        teamService.update(superUser, rename("슈퍼 수정"), 2L);
        teamService.addPlayersToTeam(superUser, 2L, List.of(new TeamRequest.TeamPlayerRegister(3L, 10)));

        assertThat(teamName(2L)).isEqualTo("슈퍼 수정");
        assertThat(teamPlayerCount(2L)).isEqualTo(2);
    }

    @Test
    void 다른_학교_소속이라도_그_학교_대회의_관리자면_그_학교_팀을_수정할_수_있다() {
        Member leagueAdminOfB = member("c-admin-b@example.com");

        assertThatCode(() -> teamService.update(leagueAdminOfB, rename("대회 관리자 수정"), 2L))
                .doesNotThrowAnyException();

        assertThat(teamName(2L)).isEqualTo("대회 관리자 수정");
    }

    @Test
    void 삭제된_대회의_관리자였던_다른_학교_매니저는_그_학교_팀을_수정할_수_없다() {
        Member formerAdmin = member("c-deleted@example.com");

        assertThatThrownBy(() -> teamService.update(formerAdmin, rename("탈취"), 2L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(teamName(2L)).isEqualTo("학교 B 팀");
    }
}
