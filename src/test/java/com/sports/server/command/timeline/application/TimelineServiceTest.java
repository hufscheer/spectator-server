package com.sports.server.command.timeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.domain.*;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@Sql(scripts = "/timeline-fixture.sql")
class TimelineServiceTest extends ServiceTest {
    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityUtils entityUtils;

    private final Long gameId = 1L;
    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 경기의_매니저가_아닌_회원이_타임라인을_등록하려고_하면_예외가_발생한다() {
        // given
        Member nonManager = entityUtils.getEntity(2L, Member.class);
        Long team1Id = 1L;
        Long team1PlayerId = 1L;

        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(team1Id, Quarter.SECOND_HALF,
                team1PlayerId, 3);

        // when & then
        assertThatThrownBy(() -> timelineService.register(nonManager, gameId, request)).isInstanceOf(
                UnauthorizedException.class);

    }

    @DisplayName("득점 타임라인을")
    @Nested
    class CreateTest {
        @Test
        void 팀1이_생성한다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;

            TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(team1Id, Quarter.SECOND_HALF,
                    team1PlayerId, 3);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(team1PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(16),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(10),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(Quarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3));

        }

        @Test
        void 팀2가_생성한다() {
            // given
            Long team2Id = 2L;
            Long team2PlayerId = 6L;

            TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(team2Id, Quarter.SECOND_HALF,
                    team2PlayerId, 5);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(team2PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(15),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(11),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(Quarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(5));
        }
    }

    @DisplayName("교체 타임라인을")
    @Nested
    class CreateReplacementTest {
        Long team1Id = 1L;
        Long team1OriginPlayerId = 1L;
        Long team1ReplacedPlayerId = 2L;

        Long team2Id = 2L;
        Long team2OriginPlayerId = 6L;
        Long team2ReplacedPlayerId = 7L;

        @Test
        void 팀1에서_생성한다() {
            // given

            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team1Id,
                    Quarter.SECOND_HALF, team1OriginPlayerId, team1ReplacedPlayerId, 3);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(() -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team1OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team1ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(Quarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true));
        }

        @Test
        void 팀2에서_생성한다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team2Id,
                    Quarter.SECOND_HALF, team2OriginPlayerId, team2ReplacedPlayerId, 3);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(() -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team2OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team2ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(Quarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true));
        }

        @Test
        void 다른_팀끼리_생성할_수_없다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team2Id,
                    Quarter.SECOND_HALF, team1OriginPlayerId, team2ReplacedPlayerId, 3);

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request)).isInstanceOf(
                    CustomException.class);
        }
    }

    @Nested
    @DisplayName("게임 진행 타임라인을")
    class GameProgressTimelineTest {
        @Test
        void 생성한다() {
            // given
            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(10, Quarter.SECOND_HALF,
                    GameProgressType.QUARTER_START);

            // when
            timelineService.register(manager, gameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertThat(actual).isInstanceOf(GameProgressTimeline.class);
        }
    }

    @DisplayName("승부차기 타임라인을")
    @Nested
    class PkTest {

        @Test
        void 생성한다() {
            // given
            Long teamId = 1L;
            Long teamPlayerId = 1L;
            int recordedAt = 10;

            TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(recordedAt, Quarter.PENALTY_SHOOTOUT,
                    teamId, teamPlayerId, true);

            // when
            timelineService.register(manager, gameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertThat(actual).isInstanceOf(PKTimeline.class);

        }
    }

    @DisplayName("경고 타임라인을")
    @Nested
    class WarningCardTest {
        @Test
        void 생성한다() {
            //given
            Long teamId = 1L;
            Long playerId = 1L;
            int recordedAt = 10;

            TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(recordedAt,
                    Quarter.SECOND_HALF, teamId, playerId, WarningCardType.YELLOW);

            //when
            timelineService.register(manager, gameId, request);

            //then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertAll(() -> Assertions.assertThat(actual).isInstanceOf(WarningCardTimeline.class),
                    () -> Assertions.assertThat(((WarningCardTimeline) actual).getWarningCardType())
                            .isEqualTo(WarningCardType.YELLOW));
        }
    }

    @DisplayName("타임라인을 삭제할 때")
    @Nested
    class DeleteTest {
        @Test
        void 마지막_타임라인을_차례로_삭제한다() {
            // given
            List<Timeline> game1Timelines = timelineFixtureRepository.findAllLatest(gameId);

            // when
            while (!game1Timelines.isEmpty()) {
                Timeline lastTimeline = game1Timelines.get(0);
                timelineService.deleteTimeline(manager, gameId, lastTimeline.getId());
                game1Timelines = timelineFixtureRepository.findAllLatest(gameId);
            }

            // then
            assertThat(timelineFixtureRepository.findAllLatest(gameId)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 3L})
        void 마지막_타임라인이_아니면_삭제할_수_없다(long timelineId) {
            // when then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, gameId, timelineId)).isInstanceOf(
                    CustomException.class);
        }
    }

    @Test
    void 경기_종료_후_타임라인을_등록하려고_하면_에러가_발생한다() {
        // given
        Long team1Id = 1L;
        Long team1PlayerId = 1L;
        Long finishedGameId = 2L;

        TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(team1Id, Quarter.SECOND_HALF,
                team1PlayerId, 3);

        // when & then
        assertThatThrownBy(() -> timelineService.register(manager, finishedGameId, request)).isInstanceOf(
                CustomException.class).hasMessage(TimelineErrorMessage.GAME_ALREADY_FINISHED);
    }

    @DisplayName("동시성 테스트: Game 상태 확인 및 점수 갱신 직렬화")
    @Nested
    class ConcurrencyTest {

        public static final int SIZE_OF_SAVED_TIMELINE_DATA = 14;
        private final int numberOfAttempts = 10;
        private final ExecutorService executorService = Executors.newFixedThreadPool(numberOfAttempts);

        // 2025-10-06 h2 환경에서는 데드락 발생하기에 무효화
        // ci 를 profile 로 두고 실행 시 통과

        @Disabled
        @Test
        void 여러_스레드에서_동시에_득점_타임라인을_등록하면_모두_성공하고_점수가_누락되지_않아야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(1L, Quarter.SECOND_HALF, 1L, 1);

            int initialScore1 = 15;
            int initialScore2 = 10;
            int expectedFinalScore1 = initialScore1 + numberOfAttempts;

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 PESSIMISTIC_WRITE 락에 의해 직렬화되어 성공해야 함")
                    .isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);

            // 3. 최종 점수 스냅샷 확인
            ScoreTimeline lastTimeline = (ScoreTimeline) actualTimelines.get(0);

            assertAll(() -> assertThat(lastTimeline.getSnapshotScore1()).as("팀1 최종 스냅샷 점수: %d", expectedFinalScore1)
                            .isEqualTo(expectedFinalScore1),
                    () -> assertThat(lastTimeline.getSnapshotScore2()).as("팀2 최종 스냅샷 점수").isEqualTo(initialScore2));
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_교체_타임라인을_등록하면_모두_성공하고_선수_상태가_일관되어야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);
            Long team1Id = 1L;
            // 1번 선수가 2번 선수로 교체되는 요청을 여러 번 시도
            Long originPlayerId = 1L;
            Long replacedPlayerId = 2L;

            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team1Id,
                    Quarter.SECOND_HALF, originPlayerId, replacedPlayerId, 1);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            // 교체가 이미 이루어진 후 또다시 동일한 교체 요청이 들어오면 실패할 수 있음.
                            // 여기서는 주로 락으로 인한 데드락/롤백 여부를 확인.
                            System.err.println("교체 타임라인 등록 중 예외 발생: " + e.getMessage());
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 최종 생성된 타임라인 수 확인
            // 교체 요청은 한 번만 성공하고, 이후 요청은 LineupPlayer 상태 때문에 실패해야 하므로,
            // 성공 카운트가 1에 가까운지, 그리고 예상치 못한 데드락이 없었는지 확인
            System.out.println("성공한 교체 타임라인 등록 횟수: " + successCount.get());
            assertThat(successCount.get()).as("LineupPlayer 상태 변경이 직렬화되어 한 번만 성공해야 함")
                    .isGreaterThanOrEqualTo(1); // 최소 한 번은 성공해야 함

            // 2. 최종 LineupPlayer 상태 확인
            ReplacementTimeline lastReplacement = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(
                            gameId).stream().max((t1, t2) -> (int) (t1.getId() - t2.getId()))
                    .orElseThrow(() -> new AssertionError("교체 타임라인이 생성되지 않았습니다."));

            assertAll(() -> assertThat(lastReplacement.getOriginLineupPlayer().isPlaying()).as(
                            "Origin Player는 최종적으로 Inactive 상태여야 함").isEqualTo(false),
                    () -> assertThat(lastReplacement.getReplacedLineupPlayer().isPlaying()).as(
                            "Replaced Player는 최종적으로 Active 상태여야 함").isEqualTo(true));
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_게임_진행_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(10, Quarter.SECOND_HALF,
                    GameProgressType.QUARTER_START);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("게임 진행 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            // GameProgressTimeline은 상태 변경을 유발하지만, 점수 갱신 같은 Race Condition 위험이 적어 모두 성공할 것으로 기대
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(GameProgressTimeline.class);
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_승부차기_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(1, Quarter.PENALTY_SHOOTOUT, 1L,
                    // teamId
                    1L, // playerId
                    true // isSuccess
            );

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("승부차기 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(PKTimeline.class);
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_경고_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(1,
                    Quarter.SECOND_HALF, 1L, // teamId
                    1L, // playerId
                    WarningCardType.YELLOW);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("경고 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(WarningCardTimeline.class);
        }
    }
}