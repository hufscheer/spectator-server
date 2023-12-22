package com.sports.server.command.report.application;

import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/report-fixture.sql")
class ReportFactoryTest extends ServiceTest {

    @Autowired
    private ReportFactory reportFactory;

    @DisplayName("신고를 생성할 때")
    @Nested
    class CreateTest {

        @Test
        void 같은_댓글의_신고가_없으면_새로_생성한다() {
            // given
            Long commentId = 1L;
            ReportRequest request = new ReportRequest(commentId);

            // when
            Report report = reportFactory.create(request);

            // then
            assertThat(report.getId()).isNull();
        }

        @Test
        void 같은_댓글의_신고가_있으면_있는걸_반환한다() {
            // given
            Long existReportComment = 3L;
            ReportRequest request = new ReportRequest(existReportComment);

            // when
            Report report = reportFactory.create(request);

            // then
            assertThat(report.getId()).isNotNull();
        }
    }
}
