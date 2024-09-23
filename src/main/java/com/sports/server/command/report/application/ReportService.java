package com.sports.server.command.report.application;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.domain.ReportState;
import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.command.report.exception.ReportErrorMessage;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportFactory reportFactory;
    private final EntityUtils entityUtils;

    public void report(final ReportRequest request) {
        Report report = reportFactory.create(request);
        reportRepository.save(report);
    }

    public void cancel(final Long leagueId, final Long cheerTalkId, final Member manager) {
        checkPermission(leagueId, manager);

        Report report = reportRepository.findByCheerTalkId(cheerTalkId);
        // 신고된 응원톡 가리기/가리기 해제에서 구현한 커스텀 에러 끌어다 사용할 예정입니당
        if (!report.getState().equals(ReportState.PENDING)) {
            throw new CustomException();
        }
        report.cancel();
    }

    private void checkPermission(final Long leagueId, final Member manager) {

        League league = entityUtils.getEntity(leagueId, League.class);

        if (!league.isManagedBy(manager)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }
}
