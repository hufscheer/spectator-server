package com.sports.server.command.report.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportRepository;
import com.sports.server.common.application.TextFileProcessor;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportProcessor {

    private final TextFileProcessor textFileProcessor;
    private final ReportRepository reportRepository;
    private static final String FILE_NAME = "static/extra_bad_words.txt";
    private static final String DELIM = ",";

    private Set<String> cachedBadWords;

    public void check(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("Report not found: " + reportId));

        if (!report.isUnchecked()) {
            return;
        }
        CheerTalk cheerTalk = report.getCheerTalk();

        if (cachedBadWords == null) {
            cachedBadWords = textFileProcessor.readFile(FILE_NAME, DELIM);
        }

        if (cachedBadWords.contains(cheerTalk.getContent())) {
            report.accept();
            return;
        }
        report.updateToPending();
    }
}
