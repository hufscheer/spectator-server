package com.sports.server.command.report.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.application.ReportProcessor;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportEvent;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.dto.ReportRequest;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Sql(scripts = "/report-fixture.sql")
class ReportEventHandlerTest extends ServiceTest {

    @Autowired
    private ReportEventHandler reportEventHandler;

    @MockBean
    protected ReportProcessor reportProcessor;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 픽스처: 응원톡 1 은 신고 전, 응원톡 3 은 미검사 신고(1번)가 있다.
     */
    @DisplayName("신고를 저장하면")
    @Nested
    class SaveReport {

        @Test
        void 새_신고는_검사를_요청한다() {
            // when
            reportService.report(new ReportRequest(1L));

            // then
            Long reportId = reportRepository.findByCheerTalkId(1L).orElseThrow().getId();
            verify(reportProcessor).check(reportId);
        }

        @Test
        void 아직_검사되지_않은_신고를_다시_신고하면_검사를_다시_요청한다() {
            // when
            reportService.report(new ReportRequest(3L));

            // then
            verify(reportProcessor).check(1L);
        }

        @Test
        void 불러온_신고를_저장하기만_해서는_검사를_요청하지_않는다() {
            // when
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Report report = reportRepository.findById(1L).orElseThrow();
                reportRepository.save(report);
            });

            // then
            verify(reportProcessor, never()).check(any(Long.class));
        }
    }

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

        // 이미 검사된 신고인지는 ReportProcessor 가 다시 읽어서 판단한다 (ReportProcessorTest)
        @Test
        void 신고_id_로_검사를_요청한다() {
            // when
            reportEventHandler.handle(new ReportEvent(REPORT_ID));

            // then
            verify(reportProcessor).check(REPORT_ID);
        }


        // 추후 람다로 이전 시 필요
        void 아직_검사가_안된_신고는_검사를_람다_서버로_요청한다() {
            // given
            given(report.isUnchecked()).willReturn(true);
            given(reportCheckClient.check(any()))
                    .willReturn(ResponseEntity.ok().build());

            // when
            reportEventHandler.handle(new ReportEvent(REPORT_ID));

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
            reportEventHandler.handle(new ReportEvent(REPORT_ID));

            // then
            verify(reportCheckClient, never()).check(any());
        }
    }
}
