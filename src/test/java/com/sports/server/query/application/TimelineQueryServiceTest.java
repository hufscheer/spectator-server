package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.domain.TimelineDeletabilityEvaluator.Reason;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/timeline-fixture.sql")
class TimelineQueryServiceTest extends ServiceTest {

    @Autowired
    private TimelineQueryService timelineQueryService;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private MemberRepository memberRepository;

    // game 6: 점수 0:0 + QUARTER_START 만 있는 정합 상태 (TimelineServiceTest.DeleteTest 와 동일 픽스처)
    private final Long replayGameId = 6L;
    private final Long replayGameTeamId = 9L;
    private final Long starter1Id = 31L;
    private final Long starter2Id = 32L;
    private final Long candidate1Id = 33L;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 진행_중_경기는_기록별_삭제_가능_여부와_사유를_내려준다() {
        // given: 득점(31, 도움 32) → 교체(31 OUT, 33 IN) → 득점(33)
        registerGoal(starter1Id, starter2Id);
        registerReplacement(starter1Id, candidate1Id);
        registerGoal(candidate1Id, null);

        // when
        List<RecordResponse> records = recordsOrderedById(replayGameId);

        // then: [QUARTER_START, 득점1, 교체, 득점2(마지막)]
        RecordResponse quarterStart = records.get(0);
        RecordResponse firstGoal = records.get(1);
        RecordResponse replacement = records.get(2);
        RecordResponse lastGoal = records.get(3);
        assertAll(
                () -> assertThat(records).hasSize(4),
                () -> assertThat(quarterStart.deletable()).isFalse(),
                () -> assertThat(quarterStart.undeletableReason())
                        .isEqualTo(TimelineErrorMessage.PROGRESS_TIMELINE_NOT_LAST),
                () -> assertThat(quarterStart.undeletableReasonCode())
                        .isEqualTo(Reason.PROGRESS_TIMELINE_NOT_LAST.name()),
                () -> assertThat(firstGoal.deletable()).isTrue(),
                () -> assertThat(firstGoal.undeletableReason()).isNull(),
                () -> assertThat(firstGoal.undeletableReasonCode()).isNull(),
                () -> assertThat(replacement.deletable()).isFalse(),
                () -> assertThat(replacement.undeletableReason())
                        .isEqualTo(TimelineErrorMessage.REPLACEMENT_PLAYER_HAS_LATER_RECORDS),
                () -> assertThat(replacement.undeletableReasonCode())
                        .isEqualTo(Reason.REPLACEMENT_PLAYER_HAS_LATER_RECORDS.name()),
                () -> assertThat(lastGoal.deletable()).isTrue(),
                () -> assertThat(lastGoal.undeletableReason()).isNull(),
                () -> assertThat(lastGoal.undeletableReasonCode()).isNull()
        );
    }

    @Test
    void 종료된_경기는_마지막_기록만_삭제_가능으로_내려준다() {
        // given: game 2 는 FINISHED
        Long finishedGameId = 2L;

        // when
        List<RecordResponse> records = recordsOrderedById(finishedGameId);

        // then
        RecordResponse last = records.get(records.size() - 1);
        List<RecordResponse> middles = records.subList(0, records.size() - 1);
        assertAll(
                () -> assertThat(middles).isNotEmpty(),
                () -> assertThat(last.deletable()).isTrue(),
                () -> assertThat(last.undeletableReason()).isNull(),
                () -> assertThat(last.undeletableReasonCode()).isNull(),
                () -> assertThat(middles).allSatisfy(record -> {
                    assertThat(record.deletable()).isFalse();
                    assertThat(record.undeletableReason())
                            .isEqualTo(TimelineErrorMessage.MIDDLE_DELETE_ONLY_WHILE_PLAYING);
                    assertThat(record.undeletableReasonCode())
                            .isEqualTo(Reason.MIDDLE_DELETE_ONLY_WHILE_PLAYING.name());
                })
        );
    }

    private List<RecordResponse> recordsOrderedById(Long gameId) {
        return timelineQueryService.getTimelines(gameId).timelines().stream()
                .flatMap(timeline -> timeline.records().stream())
                .sorted(Comparator.comparing(RecordResponse::recordId))
                .toList();
    }

    private void registerGoal(Long scorerLineupPlayerId, Long assistLineupPlayerId) {
        timelineService.register(manager, replayGameId, new TimelineRequest.RegisterSoccerScore(
                replayGameTeamId, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                scorerLineupPlayerId, 10, assistLineupPlayerId, null));
    }

    private void registerReplacement(Long originLineupPlayerId, Long replacementLineupPlayerId) {
        timelineService.register(manager, replayGameId, new TimelineRequest.RegisterReplacement(
                replayGameTeamId, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                originLineupPlayerId, replacementLineupPlayerId, 20, null));
    }
}
