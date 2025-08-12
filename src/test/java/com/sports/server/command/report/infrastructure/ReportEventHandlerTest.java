package com.sports.server.command.report.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.application.ReportProcessor;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportEvent;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@Sql(scripts = "/report-fixture.sql")
class ReportEventHandlerTest extends ServiceTest {

    @Autowired
    private ReportEventHandler reportEventHandler;

    @MockBean
    protected ReportProcessor reportProcessor;

    @DisplayName("신고 이벤트가 발생하면")
    @Nested
    class ReportEventHandleTest {

        private Report report;

        private static final String COMMENT_CONTENT = "신고될 댓글";
        private static final Long COMMENT_ID = 1L;
        private static final Long REPORT_ID = 1L;

        @BeforeEach
        void init() {
            report = mock(Report.class);
            CheerTalk cheerTalk = mock(CheerTalk.class);
            given(report.getCheerTalk()).willReturn(cheerTalk);
            given(cheerTalk.getContent()).willReturn(COMMENT_CONTENT);
            given(cheerTalk.getId()).willReturn(COMMENT_ID);
            given(report.getId()).willReturn(REPORT_ID);
        }

        @Test
        void 아직_검사가_안된_신고는_검사를_요청한다() {
            // given
            given(report.isUnchecked()).willReturn(true);
            ReportEvent reportEvent = new ReportEvent(report);

            // when
            reportEventHandler.handle(reportEvent);

            // then
            verify(reportProcessor).check(
                    any(), any()
            );
        }

        @Test
        void 이미_검사된_신고는_검사를_요청하지_않는다() {
            // given
            given(report.isUnchecked()).willReturn(false);
            ReportEvent reportEvent = new ReportEvent(report);

            // when
            reportEventHandler.handle(reportEvent);

            // then
            verify(reportProcessor, never()).check(any(CheerTalk.class), any(Report.class));
        }


        // 추후 람다로 이전 시 필요
        void 아직_검사가_안된_신고는_검사를_람다_서버로_요청한다() {
            // given
            given(report.isUnchecked()).willReturn(true);
            given(reportCheckClient.check(any()))
                    .willReturn(ResponseEntity.ok().build());

            // when
            reportEventHandler.handle(new ReportEvent(report));

            // then
            verify(reportCheckClient).check(
                    new ReportCheckRequest(COMMENT_CONTENT, COMMENT_ID, REPORT_ID)
            );
        }

        // 추후 람다로 이전 시 필요
        void 이미_검사된_신고는_람다_서버로_검사를_요청하지_않는다() {
            // given
            given(report.isUnchecked()).willReturn(false);
            given(reportCheckClient.check(any()))
                    .willReturn(ResponseEntity.ok().build());

            // when
            reportEventHandler.handle(new ReportEvent(report));

            // then
            verify(reportCheckClient, never()).check(any());
        }
    }
}
