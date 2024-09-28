package com.sports.server.command.report.presentation;


import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.dto.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{leagueId}/{cheerTalkId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long leagueId,
                                       @PathVariable Long cheerTalkId,
                                       final Member manager) {
        reportService.cancel(leagueId, cheerTalkId, manager);
        return ResponseEntity.ok().build();
    }
}
