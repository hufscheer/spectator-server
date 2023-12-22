package com.sports.server.command.report.application;

import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.command.report.dto.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportFactory reportFactory;

    public void report(final ReportRequest request) {
        Report report = reportFactory.create(request);
        reportRepository.save(report);
    }
}
