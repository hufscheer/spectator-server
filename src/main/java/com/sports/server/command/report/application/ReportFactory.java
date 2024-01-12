package com.sports.server.command.report.application;

import com.sports.server.command.comment.domain.CheerTalk;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.dto.ReportRequest;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFactory {

    private final ReportRepository reportRepository;
    private final EntityUtils entityUtils;

    public Report create(ReportRequest request) {
        CheerTalk cheerTalk = entityUtils.getEntity(request.commentId(), CheerTalk.class);
        return reportRepository.findByCheerTalk(cheerTalk)
                .orElse(new Report(cheerTalk));
    }
}
