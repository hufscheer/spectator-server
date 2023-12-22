package com.sports.server.command.report.infrastructure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface ReportCheckClient {

    @PostExchange("/deploy")
    ResponseEntity<Void> check(@RequestBody ReportCheckRequest request);
}
