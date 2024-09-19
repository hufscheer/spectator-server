package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.query.dto.response.CheerTalkResponseForManager;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/cheer-talk-fixture.sql")
public class CheerTalkQueryServiceTest extends ServiceTest {

    @Autowired
    private CheerTalkQueryService cheerTalkQueryService;

    @Autowired
    private EntityUtils entityUtils;

    @Nested
    @DisplayName("신고된 응원톡을 조회할 때")
    class TestFindReportedCheerTalksByLeagueId {

        private PageRequestDto pageRequestDto;

        private Member manager;

        @BeforeEach
        void setUp() {
            pageRequestDto = new PageRequestDto(
                    null, 10
            );
            manager = entityUtils.getEntity(1L, Member.class);
        }

        @Test
        void 신고된_응원톡만_조회된다() {
            // given
            Long leagueId = 1L;
            Long reportedCheerTalkId = 1L;

            // when
            List<CheerTalkResponseForManager> results = cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertAll(
                    () -> assertThat(results.size()).isEqualTo(1),
                    () -> assertThat(results.get(0).cheerTalkId()).isEqualTo(reportedCheerTalkId)
            );
        }

        @Test
        void 해당_리그의_응원톡만_조회된다() {
            // given
            Long leagueId = 1L;

            // when
            List<CheerTalkResponseForManager> responses = cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertThat(
                    responses.stream()
                            .map(CheerTalkResponseForManager::leagueId).toList()
            ).containsOnly(leagueId);
        }

        @Test
        void 리그의_매니저가_아닌_경우_예외가_발생한다() {
            // given
            Long leagueId = 1L;
            Member invalidManager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, invalidManager))
                    .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED)
                    .isInstanceOf(UnauthorizedException.class);
        }


    }


}
