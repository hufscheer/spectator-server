package com.sports.server.report.infrastructure;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.exception.CustomException;
import com.sports.server.report.domain.Report;
import com.sports.server.report.domain.ReportEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportEventHandler {

    private static final String REPORT_CHECK_SERVER_ERROR = "신고 검사 서버에 문제가 발생했습니다";

    private final ReportCheckClient reportCheckClient;

    @TransactionalEventListener
    @Async
    public void handle(ReportEvent event) {
        Report report = event.report();
        if (report.isUnchecked()) {
            checkReport(report);
        }
    }

    private void checkReport(Report report) {
        Comment comment = report.getComment();
        ReportCheckRequest request = new ReportCheckRequest(
                comment.getContent(), comment.getId(), report.getId()
        );
        ResponseEntity<Void> response = reportCheckClient.check(request);
        validateResponse(response);
    }

    private void validateResponse(ResponseEntity<Void> response) {
        if (response.getStatusCode().is5xxServerError()) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, REPORT_CHECK_SERVER_ERROR);
        }
    }
}
