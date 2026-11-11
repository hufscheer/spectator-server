package com.sports.server.command.player.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.common.exception.ForbiddenException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

// 선수 1, 4는 학교 A, 선수 2, 3은 학교 B. 슈퍼유저는 회원 1이다
@Sql("/organization-ownership-fixture.sql")
@DisplayName("선수 쓰기는 같은 학교 매니저, 그 학교 대회 관리자, 슈퍼유저만 할 수 있다")
class PlayerOrganizationOwnershipTest extends ServiceTest {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member(String email) {
        return memberRepository.findMemberByEmailWithOrganization(email).orElseThrow();
    }

    private String playerName(long id) {
        return jdbcTemplate.queryForObject("SELECT name FROM players WHERE id = ?", String.class, id);
    }

    private long playerCount(long id) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM players WHERE id = ?", Long.class, id);
    }

    @Test
    void 다른_학교_선수를_수정하면_403이고_선수는_그대로다() {
        assertThatThrownBy(() -> playerService.update(member("a@example.com"), 2L,
                new PlayerRequest.Update("탈취", "202201001")))
                .isInstanceOf(ForbiddenException.class);

        assertThat(playerName(2L)).isEqualTo("학교 B 선수");
    }

    @Test
    void 다른_학교_선수를_삭제하면_403이고_선수는_남는다() {
        assertThatThrownBy(() -> playerService.delete(member("a@example.com"), 2L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(playerCount(2L)).isEqualTo(1);
    }

    @Test
    void 같은_학교_매니저는_선수를_수정하고_삭제할_수_있다() {
        Member managerA = member("a@example.com");

        playerService.update(managerA, 1L, new PlayerRequest.Update("새 이름", "202101001"));
        assertThat(playerName(1L)).isEqualTo("새 이름");

        playerService.delete(managerA, 4L);
        assertThat(playerCount(4L)).isZero();
    }

    @Test
    void 슈퍼유저는_다른_학교_선수도_수정하고_삭제할_수_있다() {
        Member superUser = member("super@example.com");

        playerService.update(superUser, 2L, new PlayerRequest.Update("슈퍼 수정", "202201001"));
        assertThat(playerName(2L)).isEqualTo("슈퍼 수정");

        playerService.delete(superUser, 3L);
        assertThat(playerCount(3L)).isZero();
    }

    @Test
    void 다른_학교_소속이라도_그_학교_대회의_관리자면_그_학교_선수를_수정할_수_있다() {
        assertThatCode(() -> playerService.update(member("c-admin-b@example.com"), 2L,
                new PlayerRequest.Update("대회 관리자 수정", "202201001")))
                .doesNotThrowAnyException();

        assertThat(playerName(2L)).isEqualTo("대회 관리자 수정");
    }
}
