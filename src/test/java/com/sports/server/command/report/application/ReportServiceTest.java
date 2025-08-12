package com.sports.server.command.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.ReportFixtureRepository;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.domain.ReportState;
import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@Sql(scripts = "/report-fixture.sql")
class ReportServiceTest extends ServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportFixtureRepository reportFixtureRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    ReportRepository reportRepository;

    @DisplayName("신고를 저장할 때")
    @Nested
    class CreateReport {

        @Test
        void 블락되지_않은_응원톡의_신고는_저장한다() {
            // given
            Long commentId = 1L;
            ReportRequest request = new ReportRequest(commentId);

            // when
            reportService.report(request);

            // then
            Optional<Report> actual = reportFixtureRepository.findByCheerTalkId(commentId);
            assertThat(actual).isPresent();
        }

        @Test
        void 존재하지_않은_응원톡은_저장하지_않는다() {
            // given
            Long notExistComment = 100L;
            ReportRequest request = new ReportRequest(notExistComment);

            // when then
            assertThatThrownBy(() -> reportService.report(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("CheerTalk을(를) 찾을 수 없습니다");
        }

        @Test
        void 블락된_응원톡은_저장하지_않는다() {
            // given
            Long blockedComment = 2L;
            ReportRequest request = new ReportRequest(blockedComment);

            // when then
            assertThatThrownBy(() -> reportService.report(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 블락된 응원톡은 신고할 수 없습니다.");
        }

        @Test
        void 같은_응원톡으로_신고하면_중복_저장되지_않는다() {
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

    @DisplayName("신고를 무효 처리할 때")
    @Nested
    class cancelReport {

        Long leagueId;
        Long cheerTalkId;
        Member manager;

        @BeforeEach
        void setUp() {
            leagueId = 1L;
        }

        @Test
        void 정상적으로_응원톡_신고를_무효처리한다() {
            // given
            cheerTalkId = 4L;
            manager = entityUtils.getEntity(1L, Member.class);

            // when
            reportService.cancel(leagueId, cheerTalkId, manager);

            // then
            Report report = reportRepository.findByCheerTalkId(cheerTalkId);
            assertThat(report.getState()).isEqualTo(ReportState.INVALID);
        }

        @Test
        void 해당_응원톡_소속_리그의_관리자가_아니면_예외가_발생한다() {
            // given
            cheerTalkId = 4L;
            manager = entityUtils.getEntity(2L, Member.class);

            // when & then
            assertThatThrownBy(() -> reportService.cancel(leagueId, cheerTalkId, manager))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}
