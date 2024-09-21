package com.sports.server.command.report.infrastructure;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.report.application.ReportProcessor;
import com.sports.server.command.report.domain.Report;
import com.sports.server.command.report.domain.ReportEvent;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportEventHandler {

    private final ReportProcessor reportProcessor;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(ReportEvent event) throws IOException {
        Report report = event.report();
        if (report.isUnchecked()) {
            checkReport(report);
        }
    }

    private void checkReport(Report report) throws IOException {
        CheerTalk cheerTalk = report.getCheerTalk();
        reportProcessor.check(cheerTalk, report);
    }

// 추후 람다로 이전 시 필요한 메서드들

//    @TransactionalEventListener
//    @Async("asyncThreadPool")
//    public void handle(ReportEvent event) {
//        Report report = event.report();
//        if (report.isUnchecked()) {
//            checkReport(report);
//        }
//    }
//
//    private void checkReport(Report report) {
//        CheerTalk cheerTalk = report.getCheerTalk();
//        ReportCheckRequest request = new ReportCheckRequest(
//                cheerTalk.getContent(), cheerTalk.getId(), report.getId()
//        );
//        ResponseEntity<Void> response = reportCheckClient.check(request);
//        validateResponse(response);
//    }
//
//    private void validateResponse(ResponseEntity<Void> response) {
//        if (response.getStatusCode().is5xxServerError()) {
//            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ReportErrorMessage.REPORT_CHECK_SERVER_ERROR);
//        }
//    }
}
