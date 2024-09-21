package com.sports.server.command.report.application;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.domain.Report;
import com.sports.server.common.application.TextFileProcessor;
import java.io.IOException;
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

    public void check(CheerTalk cheerTalk, Report report) throws IOException {
        Set<String> badWords = textFileProcessor.readFile(FILE_NAME, DELIM);

        if (badWords.contains(cheerTalk.getContent())) {
            report.updateToValid();
            cheerTalk.block();
            return;
        }
        report.updateToPending();
    }
}
