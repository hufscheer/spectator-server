package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.dto.response.CheerTalkResponse;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    private Member admin1;
    private Member admin2;

    @BeforeEach
    void setUp() {
        pageRequestDto = new PageRequestDto(
                null, 10
        );
        admin1 = entityUtils.getEntity(1L, Member.class);
        admin2 = entityUtils.getEntity(2L, Member.class);
    }

    @Nested
    @DisplayName("관리자별 신고된 응원톡을 조회할 때")
    class TestFindReportedCheerTalksByAdmin {

        @Test
        void 관리자1의_신고된_응원톡만_조회된다() {
            // given
            Long reportedCheerTalkId1 = 18L;
            Long reportedCheerTalkId2 = 1L;

            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getReportedCheerTalksByAdmin(
                    pageRequestDto, admin1);

            // then
            assertAll(
                    () -> assertThat(results.size()).isEqualTo(2),
                    () -> assertThat(results.get(0).cheerTalkId()).isEqualTo(reportedCheerTalkId1),
                    () -> assertThat(results.get(1).cheerTalkId()).isEqualTo(reportedCheerTalkId2),
                    () -> assertThat(results.stream().map(CheerTalkResponse.ForManager::leagueId).toList()).containsOnly(1L)
            );
        }

        @Test
        void 관리자2의_신고된_응원톡만_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getReportedCheerTalksByAdmin(
                    pageRequestDto, admin2);

            // then
            // 관리자2는 신고된 응원톡이 없으므로 빈 리스트가 반환되어야 함
            assertThat(responses).isEmpty();
        }

        @Test
        void 다른_관리자의_신고된_응원톡은_조회되지_않는다() {
            // when
            List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getReportedCheerTalksByAdmin(
                    pageRequestDto, admin1);

            // then
            // 관리자1의 신고된 응원톡만 조회되어야 하고, 관리자2의 신고된 응원톡은 조회되지 않아야 함
            assertThat(responses.stream().map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                .containsOnly(18L, 1L); // 관리자1의 신고된 응원톡들만
        }

	}

	@Nested
	@DisplayName("관리자별 가려진 응원톡 전체 조회")
	class TestFindBlockedCheerTalksByAdmin {

		@Test
		void 관리자1의_가려진_응원톡만_조회된다() {
			// given
			List<Long> blockedCheerTalkIds = List.of(23L, 14L);

			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByAdmin(
				pageRequestDto, admin1);

			// then
			assertAll(
				() -> assertThat(responses.size()).isEqualTo(2),
				() -> assertThat(responses.stream()
                        .map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                        .containsAll(blockedCheerTalkIds),
				() -> assertThat(responses.stream()
                        .map(CheerTalkResponse.ForManager::leagueId).toList())
                        .containsOnly(1L)
			);
		}

		@Test
		void 관리자2의_가려진_응원톡만_조회된다() {
			// given
			List<Long> expectedBlockedCheerTalkIds = List.of(29L, 26L); // 리그3과 리그2의 blocked 응원톡들

			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByAdmin(
				pageRequestDto, admin2);

			// then
			assertAll(
				() -> assertThat(responses.size()).isEqualTo(2),
				() -> assertThat(
					responses.stream()
                            .map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                        .containsAll(expectedBlockedCheerTalkIds),
				() -> assertThat(responses.stream()
                        .map(CheerTalkResponse.ForManager::leagueId)
                        .collect(Collectors.toSet()))
                        .containsExactlyInAnyOrder(2L, 3L)
			);
		}

		@Test
		void 다른_관리자의_응원톡은_조회되지_않는다() {
			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByAdmin(
				pageRequestDto, admin1);

			// then
			// 관리자1의 응원톡만 조회되어야 하고, 관리자2의 응원톡들(26L, 29L)은 조회되지 않아야 함
			assertThat(responses.stream().map(CheerTalkResponse.ForManager::cheerTalkId).toList())
				.doesNotContain(26L, 29L);
		}

		@Test
		void 관리자2가_여러_리그를_관리할_때_모든_리그의_블락된_응원톡이_조회된다() {
			// when
			List<CheerTalkResponse.ForManager> responses = cheerTalkQueryService.getBlockedCheerTalksByAdmin(
				pageRequestDto, admin2);

			// then
			assertAll(
				() -> assertThat(responses.size()).isEqualTo(2),
				() -> assertThat(responses.stream()
                        .map(CheerTalkResponse.ForManager::leagueId).collect(Collectors.toSet()))
                        .containsExactlyInAnyOrder(2L, 3L),
				() -> assertThat(responses.stream()
                        .map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                        .containsExactlyInAnyOrder(26L, 29L)
			);
		}
	}

    @Nested
    @DisplayName("관리자별 블락되지 않은 응원톡을 전체 조회 할 때")
    class TestFindUnblockedCheerTalksByAdmin {

        @Test
        void 관리자1의_응원톡이_최신순으로_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getUnblockedCheerTalksByAdmin(
                    pageRequestDto, admin1);

            // then
            assertAll(
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::cheerTalkId)
                            .containsExactly(22L, 21L, 20L, 19L, 18L, 17L, 16L, 15L, 13L, 12L),
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::createdAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> assertThat(results.stream().map(CheerTalkResponse.ForManager::leagueId).toList()).containsOnly(1L)
            );
        }

        @Test
        void 관리자2의_응원톡이_최신순으로_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> results = cheerTalkQueryService.getUnblockedCheerTalksByAdmin(
                    pageRequestDto, admin2);

            // then
            assertAll(
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::cheerTalkId)
                            .containsExactly(28L, 27L, 25L, 24L), // 두번째 관리자의 모든 unblocked 응원톡들 (리그3 + 리그2)
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::createdAt)
                            .isSortedAccordingTo(Comparator.reverseOrder()),
                    () -> assertThat(results.stream()
                            .map(CheerTalkResponse.ForManager::leagueId).toList())
                            .contains(2L, 3L)
            );
        }

        @Test
        void 차단되지_않은_응원톡만_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> results =
                    cheerTalkQueryService.getUnblockedCheerTalksByAdmin(pageRequestDto, admin1);

            // then
            assertThat(
                    results.stream().map(CheerTalkResponse.ForManager::isBlocked)
            ).containsOnly(false);
        }

        @Test
        void 다른_관리자의_응원톡은_조회되지_않는다() {
            // when
            List<CheerTalkResponse.ForManager> responses
                    = cheerTalkQueryService.getUnblockedCheerTalksByAdmin(pageRequestDto, admin1);

            // then
            // 관리자1의 응원톡만 조회되어야 하고, 관리자2의 응원톡들(24L, 25L, 27L, 28L)은 조회되지 않아야 함
            assertThat(responses.stream()
                    .map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                    .doesNotContain(24L, 25L, 27L, 28L);
        }

        @Test
        void 관리자가_여러_리그를_관리할_때_모든_리그의_응원톡이_조회된다() {
            // when
            List<CheerTalkResponse.ForManager> results =
                    cheerTalkQueryService.getUnblockedCheerTalksByAdmin(pageRequestDto, admin2);

            // then
            assertAll(
                    () -> assertThat(results.size()).isEqualTo(4),
                    () -> assertThat(results.stream()
                            .map(CheerTalkResponse.ForManager::leagueId).toList())
                            .contains(2L, 3L),
                    () -> assertThat(results.stream()
                            .map(CheerTalkResponse.ForManager::cheerTalkId).toList())
                            .containsExactlyInAnyOrder(24L, 25L, 27L, 28L)
            );
        }

        @Test
        void 페이지네이션이_정상_작동한다() {
            // given
            PageRequestDto request = new PageRequestDto(null, 3);

            // when
            List<CheerTalkResponse.ForManager> results =
                    cheerTalkQueryService.getUnblockedCheerTalksByAdmin(request, admin1);

            // then
            assertAll(
                    () -> assertThat(results.size()).isEqualTo(3),
                    () -> assertThat(results)
                            .map(CheerTalkResponse.ForManager::cheerTalkId)
                            .containsExactly(22L, 21L, 20L),
                    () -> assertThat(results.stream()
                            .map(CheerTalkResponse.ForManager::leagueId).toList())
                            .containsOnly(1L)
            );
        }
    }
}
