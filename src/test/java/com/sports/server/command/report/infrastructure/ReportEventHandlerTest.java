package com.sports.server.command.report.infrastructure;

import com.sports.server.command.comment.domain.Comment;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportEvent;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Sql(scripts = "/report-fixture.sql")
class ReportEventHandlerTest extends ServiceTest {

    @Autowired
    private ReportEventHandler reportEventHandler;

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
            Comment comment = mock(Comment.class);
            given(report.getComment()).willReturn(comment);
            given(comment.getContent()).willReturn(COMMENT_CONTENT);
            given(comment.getId()).willReturn(COMMENT_ID);
            given(report.getId()).willReturn(REPORT_ID);
        }

        @Test
        void 아직_검사가_안된_신고는_검사를_요청한다() {
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

        @Test
        void 이미_검사된_신고는_검사를_요청하지_않는다() {
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
