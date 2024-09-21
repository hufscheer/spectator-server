package com.sports.server.command.report.application;

import static com.sports.server.support.fixture.FixtureMonkeyUtils.entityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportState;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportProcessorTest extends ServiceTest {

    @Autowired
    private ReportProcessor reportProcessor;

    @ParameterizedTest
    @ValueSource(strings = {"개같아", "뒤질", "ezr"})
    void 욕설이_포함된_댓글인_경우_신고의_상태가_VALID가_되고_댓글이_블락된다(String badWord) {

        // given
        CheerTalk cheerTalk = entityBuilder(CheerTalk.class)
                .set("content", badWord).sample();

        Report report = entityBuilder(Report.class)
                .set("cheerTalk", cheerTalk)
                .set("state", ReportState.UNCHECKED)
                .sample();

        // when
        reportProcessor.check(cheerTalk, report);

        // then
        assertAll(
                () -> assertThat(report.getState()).isEqualTo(ReportState.VALID),
                () -> assertThat(cheerTalk.isBlocked()).isEqualTo(true)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"욕이 아님", "욕아님"})
    void 욕설이_포함되지_않은_경우_신고의_상태가_PENDING이_된다(String badWord) {

        // given
        CheerTalk cheerTalk = entityBuilder(CheerTalk.class)
                .set("content", badWord).sample();

        Report report = entityBuilder(Report.class)
                .set("cheerTalk", cheerTalk)
                .set("state", ReportState.UNCHECKED)
                .sample();

        // when
        reportProcessor.check(cheerTalk, report);

        // then
        assertThat(report.getState()).isEqualTo(ReportState.PENDING);
    }
}
