package com.sports.server.command.league.application;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-fixture.sql")
public class LeagueServiceTest extends ServiceTest {
    @Autowired
    private LeagueService leagueService;

    @Autowired
    private EntityUtils entityUtils;

    @Nested
    @DisplayName("리그를 삭제할 떄")
    class LeagueDeleteTest {
        @Test
        void isDeleted가_true가_된다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            leagueService.delete(manager, leagueId);

            // then
            League league = entityUtils.getEntity(leagueId, League.class);
            assertThat(league.isDeleted()).isEqualTo(true);
        }

        @Test
        void 권한이_없는_멤버는_리그를_삭제할_수_없다() {
            // given
            Long leagueId = 1L;
            Member manager = entityUtils.getEntity(3L, Member.class);

            // when & then
            assertThatThrownBy(
                    () -> leagueService.delete(manager, leagueId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED);

        }
    }
}
