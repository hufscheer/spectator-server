package com.sports.server.query.application;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.MemberResponse;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/member-fixture.sql")
public class MemberQueryServiceTest extends ServiceTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private EntityUtils entityUtils;

    @Test
    void 멤버의_정보가_올바르게_조회된다() {

        // given
        Member member = entityUtils.getEntity(1L, Member.class);

        // when
        MemberResponse memberInfoDto = memberQueryService.getMemberInfo(member);

        // then
        assertAll(
                () -> assertEquals(memberInfoDto.email(), "john@example.com"),
                () -> assertEquals(memberInfoDto.nameOfOrganization(), "축구 협회")
        );

    }

}
