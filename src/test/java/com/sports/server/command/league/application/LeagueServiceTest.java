package com.sports.server.command.league.application;


import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
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
            Member manager = entityUtils.getEntity(3L, Member.class);

            // when & then
            assertThatThrownBy(
                    () -> leagueService.delete(manager, leagueId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED);

        }
    }
}
