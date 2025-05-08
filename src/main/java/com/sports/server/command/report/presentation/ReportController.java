package com.sports.server.command.report.presentation;


import com.sports.server.command.member.domain.Member;
import com.sports.server.command.report.application.ReportService;
import com.sports.server.command.report.dto.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void report(@RequestBody ReportRequest request) {
        reportService.report(request);
    }

    @PatchMapping("/{leagueId}/{cheerTalkId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancel(@PathVariable Long leagueId,
                                       @PathVariable Long cheerTalkId,
                                       final Member manager) {
        reportService.cancel(leagueId, cheerTalkId, manager);
    }
}
