package com.sports.server.command.report.application;

import com.sports.server.command.report.ReportFixtureRepository;
import com.sports.server.common.exception.CustomException;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.dto.request.ReportRequest;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(scripts = "/report-fixture.sql")
class ReportServiceTest extends ServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportFixtureRepository reportFixtureRepository;

    @DisplayName("신고를 저장할 때")
    @Nested
    class CreateReport {

        @Test
        void 블락되지_않은_댓글의_신고는_저장한다() {
            // given
            Long commentId = 1L;
            ReportRequest request = new ReportRequest(commentId);

            // when
            reportService.report(request);

            // then
            Optional<Report> actual = reportFixtureRepository.findByCommentId(commentId);
            assertThat(actual).isPresent();
        }

        @Test
        void 존재하지_않은_댓글은_저장하지_않는다() {
            // given
            Long notExistComment = 100L;
            ReportRequest request = new ReportRequest(notExistComment);

            // when then
            assertThatThrownBy(() -> reportService.report(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("Comment을(를) 찾을 수 없습니다");
        }

        @Test
        void 블락된_댓글은_저장하지_않는다() {
            // given
            Long blockedComment = 2L;
            ReportRequest request = new ReportRequest(blockedComment);

            // when then
            assertThatThrownBy(() -> reportService.report(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 블락된 댓글은 신고할 수 없습니다.");
        }

        @Test
        void 같은_댓글로_신고하면_중복_저장되지_않는다() {
            // given
            Long commentId = 1L;
            ReportRequest request = new ReportRequest(commentId);
            reportService.report(request);
            long beforeReport = reportFixtureRepository.count();

            // when
            reportService.report(request);

            // when
            long afterReport = reportFixtureRepository.count();
            assertThat(afterReport).isEqualTo(beforeReport);
        }
    }
}
