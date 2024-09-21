package com.sports.server.command.report.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.domain.Report;
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
    private static final String FILE_NAME = "static/extra_bad_words.txt";
    private static final String DELIM = ",";

    private Set<String> cachedBadWords;

    public void check(CheerTalk cheerTalk, Report report) {

        if (cachedBadWords == null) {
            cachedBadWords = textFileProcessor.readFile(FILE_NAME, DELIM);
        }

        if (cachedBadWords.contains(cheerTalk.getContent())) {
            report.updateToValid();
            cheerTalk.block();
            return;
        }
        report.updateToPending();
    }
}
