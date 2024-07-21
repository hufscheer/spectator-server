package com.sports.server.command.timeline.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

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

            TimelineDto.RegisterScore request = new TimelineDto.RegisterScore(
                    team1Id,
                    quarterId,
                    team1PlayerId,
                    3
            );

            // when
            timelineService.registerScore(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId)
                    .get(0);

            assertThat(actual.getScorer().getId()).isEqualTo(team1PlayerId);

            assertThat(actual.getSnapshotScore1()).isEqualTo(16);
            assertThat(actual.getSnapshotScore2()).isEqualTo(10);

            assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId);
            assertThat(actual.getRecordedAt()).isEqualTo(3);
        }

        @Test
        void 팀2가_생성한다() {
            // given
            Long team2Id = 2L;
            Long team2PlayerId = 6L;

            TimelineDto.RegisterScore request = new TimelineDto.RegisterScore(
                    team2Id,
                    quarterId,
                    team2PlayerId,
                    5
            );

            // when
            timelineService.registerScore(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId)
                    .get(0);

            assertThat(actual.getScorer().getId()).isEqualTo(team2PlayerId);

            assertThat(actual.getSnapshotScore1()).isEqualTo(15);
            assertThat(actual.getSnapshotScore2()).isEqualTo(11);

            assertThat(actual.getRecordedQuarter().getId()).isEqualTo(quarterId);
            assertThat(actual.getRecordedAt()).isEqualTo(5);
        }
    }
}
