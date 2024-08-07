package com.sports.server.command.timeline;

import com.sports.server.query.application.timeline.TimelineQueryService;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/record-migration-fixture.sql")
class TimelineMigrationTest extends ServiceTest {
    @Autowired
    private TimelineMigration timelineMigration;

    @Autowired
    private TimelineQueryService timelineQueryService;

    @Test
    void 마이그레이션_테스트() {
        // when
        timelineMigration.migrateTimeline();

        // then
        Long gameId = 1L;

        List<RecordResponse> timelineByRecord = timelineQueryService.getTimeline(gameId)
                .stream().flatMap(timeline -> timeline.records().stream())
                .toList();
        List<RecordResponse> timelineByTimeline = timelineQueryService.getTimelines(gameId)
                .stream().flatMap(timeline -> timeline.records().stream())
                .toList();

        assertThat(timelineByRecord).usingRecursiveComparison()
                .ignoringFields(
                        "scoreRecord.scoreRecordId",
                        "replacementRecord.replacementRecordId",
                        "quarter"
                )
                .isEqualTo(timelineByTimeline);
    }
}
