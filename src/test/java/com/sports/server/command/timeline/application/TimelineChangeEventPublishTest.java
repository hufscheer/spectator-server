package com.sports.server.command.timeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineChangedEvent;
import com.sports.server.command.timeline.domain.TimelineChangedEvent.ChangeType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

@RecordApplicationEvents
@Sql(scripts = "/timeline-fixture.sql")
@DisplayName("관객 화면 갱신용 타임라인 변경 알림은")
class TimelineChangeEventPublishTest extends ServiceTest {

    private static final Long GAME_ID = 6L;
    private static final Long GAME_TEAM_ID = 9L;
    private static final Long STARTER_1 = 31L;
    private static final Long STARTER_2 = 32L;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplicationEvents events;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 기록을_등록하면_발행된다() {
        // when
        registerGoal(STARTER_1);

        // then
        assertThat(changeTypes()).containsExactly(ChangeType.CREATED);
    }

    @Test
    void 마지막_기록을_삭제하면_발행된다() {
        // given
        registerGoal(STARTER_1);
        Timeline last = timelineFixtureRepository.findAllLatest(GAME_ID).get(0);

        // when
        timelineService.deleteTimeline(manager, GAME_ID, last.getId());

        // then
        assertThat(changeTypes()).containsExactly(ChangeType.CREATED, ChangeType.DELETED);
    }

    @Test
    void 중간_기록을_삭제해도_발행된다() {
        // given
        registerGoal(STARTER_1);
        Timeline first = timelineFixtureRepository.findAllLatest(GAME_ID).get(0);
        registerGoal(STARTER_2);

        // when
        timelineService.deleteTimeline(manager, GAME_ID, first.getId());

        // then
        assertThat(changeTypes())
                .containsExactly(ChangeType.CREATED, ChangeType.CREATED, ChangeType.DELETED);
    }

    @Test
    void 삭제가_거부되면_발행되지_않는다() {
        // given: 진행 기록은 중간 삭제가 거부된다
        registerGoal(STARTER_1);
        List<Timeline> timelines = timelineFixtureRepository.findAllLatest(GAME_ID);
        Timeline quarterStart = timelines.get(timelines.size() - 1);

        // when
        assertThatThrownBy(() -> timelineService.deleteTimeline(manager, GAME_ID, quarterStart.getId()))
                .isInstanceOf(CustomException.class);

        // then
        assertThat(changeTypes()).containsExactly(ChangeType.CREATED);
    }

    @Test
    void 그_경기의_식별자를_담는다() {
        // when
        registerGoal(STARTER_1);

        // then
        assertThat(events.stream(TimelineChangedEvent.class))
                .extracting(TimelineChangedEvent::gameId)
                .containsExactly(GAME_ID);
    }

    private List<ChangeType> changeTypes() {
        return events.stream(TimelineChangedEvent.class)
                .map(TimelineChangedEvent::changeType)
                .toList();
    }

    private void registerGoal(Long scorerLineupPlayerId) {
        timelineService.register(manager, GAME_ID, new TimelineRequest.RegisterSoccerScore(
                GAME_TEAM_ID, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                scorerLineupPlayerId, 10, null, null));
    }
}
