package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
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

    private PageRequestDto pageRequestDto;

    private Member manager;

    @BeforeEach
    void setUp() {
        pageRequestDto = new PageRequestDto(
                null, 10
        );
        manager = entityUtils.getEntity(1L, Member.class);
    }

    @Nested
    @DisplayName("신고된 응원톡을 조회할 때")
    class TestFindReportedCheerTalksByLeagueId {

        @Test
        void 신고된_응원톡만_조회된다() {
            // given
            Long leagueId = 1L;
            Long reportedCheerTalkId1 = 18L;
            Long reportedCheerTalkId2 = 1L;

            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertAll(
                    () -> assertThat(results.size()).isEqualTo(2),
                    () -> assertThat(results.get(0).cheerTalkId()).isEqualTo(reportedCheerTalkId1),
                    () -> assertThat(results.get(1).cheerTalkId()).isEqualTo(reportedCheerTalkId2)
            );
        }

        @Test
        void 해당_리그의_응원톡만_조회된다() {
            // given
            Long leagueId = 1L;

            // when
            List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertThat(
                    responses.stream()
                            .map(CheerTalkResponse.ForManager::leagueId).toList()
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
                    .isInstanceOf(UnauthorizedException.class);
        }

	}

	@Nested
	@DisplayName("가려진 응원톡 전체 조회")
	class TestFindBlockedCheerTalksByLeagueId {

		@Test
		void 가려진_응원톡만_조회된다() {
			// given
			Long leagueId = 1L;
			List<Long> blockedCheerTalkIds = List.of(23L, 14L);

			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByLeagueId(
				leagueId, pageRequestDto, manager);

			// then
			assertAll(
				() -> assertThat(responses.size()).isEqualTo(2),
				() -> assertThat(
					responses.stream().map(CheerTalkResponse.ForManager::cheerTalkId).toList()).containsAll(blockedCheerTalkIds)
			);
		}

		@Test
		void 해당_리그의_응원톡만_조회된다() {
			// given
			Long leagueId = 1L;

			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByLeagueId(
				leagueId, pageRequestDto, manager);

			// then
			assertThat(responses.stream().map(CheerTalkResponse.ForManager::leagueId).toList()).containsOnly(leagueId);
		}

		@Test
		void 리그의_매니저가_아닌_경우_예외가_발생한다() {
			// given
			Long leagueId = 1L;
			Member invalidManager = entityUtils.getEntity(2L, Member.class);

			// when
			ThrowableAssert.ThrowingCallable actual = () -> cheerTalkQueryService.getBlockedCheerTalksByLeagueId(
				leagueId, pageRequestDto, invalidManager);

			// when & then
			assertThatThrownBy(actual)
				.isInstanceOf(UnauthorizedException.class);
		}
	}
    @Nested
    @DisplayName("블락되지 않은 리그의 응원톡을 전체 조회 할 때")
    class TestFindUnblockedCheerTalksByLeagueId {

        private Long leagueId;

        @BeforeEach
        void setUp() {
            leagueId = 1L;
        }

        @Test
        void 최신순으로_조회된다() {

            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getUnblockedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertAll(
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::cheerTalkId)
                            .containsExactly(22L, 21L, 20L, 19L, 18L, 17L, 16L, 15L, 13L, 12L),
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::createdAt)
                            .isSortedAccordingTo(Comparator.reverseOrder())
            );
        }


        @Test
        void 차단되지_않은_응원톡만_조회된다() {
            // given
            Long leagueId = 1L;

            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getUnblockedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertThat(
                    results.stream().map(CheerTalkResponse.ForManager::isBlocked)
            ).containsOnly(false);
        }

        @Test
        void 해당_리그의_응원톡만_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getReportedCheerTalksByLeagueId(
                    leagueId, pageRequestDto, manager);

            // then
            assertThat(
                    responses.stream()
                            .map(CheerTalkResponse.ForManager::leagueId).toList()
            ).containsOnly(leagueId);
        }
    }
}
