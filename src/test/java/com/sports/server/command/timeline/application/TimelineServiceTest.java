package com.sports.server.command.timeline.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.domain.*;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql(scripts = "/timeline-fixture.sql")
class TimelineServiceTest extends ServiceTest {
    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final Long gameId = 1L;
    private final Long quarterId = 3L;
    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com")
                .orElseThrow();
    }

    @DisplayName("득점 타임라인을")
    @Nested
    class CreateTest {
        @Test
        void 팀1이_생성한다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;

            TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                    team1Id,
                    quarterId,
                    team1PlayerId,
                    3
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId)
                    .get(0);

            assertAll(
                    () -> assertThat(actual.getScorer().getId()).isEqualTo(team1PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(16),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(10),
                    () -> assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3)
            );

        }

        @Test
        void 팀2가_생성한다() {
            // given
            Long team2Id = 2L;
            Long team2PlayerId = 6L;

            TimelineRequest.RegisterScore request = new TimelineRequest.RegisterScore(
                    team2Id,
                    quarterId,
                    team2PlayerId,
                    5
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId)
                    .get(0);

            assertAll(
                    () -> assertThat(actual.getScorer().getId()).isEqualTo(team2PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(15),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(11),
                    () -> assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(5)
            );
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

            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    team1Id,
                    quarterId,
                    team1OriginPlayerId,
                    team1ReplacedPlayerId,
                    3
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(
                    () -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team1OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team1ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true)
            );
        }

        @Test
        void 팀2에서_생성한다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    team2Id,
                    quarterId,
                    team2OriginPlayerId,
                    team2ReplacedPlayerId,
                    3
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(
                    () -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team2OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team2ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true)
            );
        }

        @Test
        void 다른_팀끼리_생성할_수_없다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    team2Id,
                    quarterId,
                    team1OriginPlayerId,
                    team2ReplacedPlayerId,
                    3
            );

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("게임 진행 타임라인을")
    class GameProgressTimelineTest {
        @Test
        void 생성한다() {
            // given
            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(
                    10,
                    3L,
                    GameProgressType.QUARTER_START
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertThat(actual).isInstanceOf(GameProgressTimeline.class);
        }
    }

    @DisplayName("타임라인을 삭제할 때")
    @Nested
    class DeleteTest {
        @Test
        void 마지막_타임라인을_차례로_삭제한다() {
            // given
            long lastId = 6L;

            // when
            for (long i = lastId; i > 0; i--) {
                timelineService.deleteTimeline(manager, gameId, i);
            }

            // then
            assertThat(timelineFixtureRepository.findAll()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 3L})
        void 마지막_타임라인이_아니면_삭제할_수_없다(long timelineId) {
            // when then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, gameId, timelineId))
                    .isInstanceOf(CustomException.class);
        }
    }
}
