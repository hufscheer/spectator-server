package com.sports.server.command.report.presentation;


import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.dto.request.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> report(@RequestBody ReportRequest request) {
        reportService.report(request);
        return ResponseEntity.noContent()
                .build();
    }
}
